package com.thoughtworks.qdox.model.expression;

import com.thoughtworks.qdox.library.ClassLibrary;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.type.TypeResolver;

import java.util.List;
import java.util.StringTokenizer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class FieldRef
    implements AnnotationValue
{
    private final int[] parts;

    private final String name;

    private JavaClass declaringClass;
    
    private ClassLibrary classLibrary;

    private JavaField field;

    private int fieldIndex = -1;

    /**
     * the <code>TypeResolver</code> object of the declaring class,
     * used to resolve of value of static final values of other classes
     * which is part of the final url of <code>RequestMapping</code>
     */
    private TypeResolver typeResolver;

    /**
     * @param name the field name, not <code>null</code>
     */
    public FieldRef( String name )
    {
        this.name = name;

        int length = new StringTokenizer( name, "." ).countTokens();
        this.parts = new int[length + 1];
        this.parts[0] = -1;

        for ( int i = 1; i < length; ++i )
        {
            this.parts[i] = name.indexOf( '.', this.parts[i - 1] + 1 );
        }

        this.parts[length] = name.length();
    }

    /**
     * create with the name and the <code>TypeResolver</code> object of the declaring class
     * @param name the field name of the declaring class or any other class, not <code>null</code>
     * @param typeResolver
     */
    public FieldRef(String name, TypeResolver typeResolver) {
        this(name);
        this.typeResolver = typeResolver;
    }

    public String getName()
    {
        return name;
    }

    public String getNamePrefix( int end )
    {
        return name.substring( 0, parts[end + 1] );
    }

    public String getNamePart( int index )
    {
        return name.substring( parts[index] + 1, parts[index + 1] );
    }

    public int getPartCount()
    {
        return parts.length - 1;
    }

    /** {@inheritDoc} */
    public Object accept( ExpressionVisitor visitor )
    {
        return visitor.visit( this );
    }

    /** {@inheritDoc} */
    public String getParameterValue()
    {
        return getName();
    }

    @Override
    public String toString()
    {
        JavaField field = getField();
        if ( field != null && !getDeclaringClass().equals( field.getDeclaringClass() ) )
        {
            return field.getDeclaringClass().getCanonicalName() + "." + field.getName();
        }
        else
        {
            return name; 
        }
    }

    public void setDeclaringClass( JavaClass declaringClass )
    {
        this.declaringClass = declaringClass;
    }
    
    public void setClassLibrary( ClassLibrary classLibrary )
    {
        this.classLibrary = classLibrary;
    }

    public String getClassPart()
    {
        String result = null;

        if ( getField() != null )
        {
            result = name.substring( 0, parts[fieldIndex] );
        }

        return result;
    }

    public String getFieldPart()
    {
        String result = null;

        if ( getField() != null )
        {
            result = name.substring( parts[fieldIndex] + 1 );
        }

        return result;
    }

    protected JavaField resolveField( JavaClass javaClass, int start, int end )
    {
        JavaField field = null;

        for ( int i = start; i < end; ++i )
        {
            field = javaClass.getFieldByName( getNamePart( i ) );

            if ( field != null )
            {
                break;
            }
        }

        return field;
    }

    public JavaField getField()
    {
        JavaClass declaringClass = getDeclaringClass();
        if ( fieldIndex < 0 && declaringClass != null)
        {
            field = resolveField( declaringClass, 0, parts.length - 1 );
            fieldIndex = 0;

            if ( field == null )
            {
                ClassLibrary classLibrary = getClassLibrary();
                if ( classLibrary != null )
                {
                    for ( int i = 0; i < parts.length - 1; ++i )
                    {
                        String className = getNamePrefix( i );

                        if ( classLibrary.hasClassReference( className ) )
                        {
                            JavaClass javaClass = classLibrary.getJavaClass( className );
                            fieldIndex = i + 1;
                            field = resolveField( javaClass, i + 1, parts.length - 1 );
                            break;
                        }
                    }
                }
            }
            
            if ( field == null )
            {
                ClassLibrary classLibrary = getClassLibrary();
                if ( classLibrary != null )
                {
                    List<String> imports = getDeclaringClass().getSource().getImports();
                    for ( String i : imports )
                    {
                        if ( i.startsWith( "static" ) )
                        {
                            String member = i.substring( i.lastIndexOf( '.' ) + 1 );
                            if ( "*".equals( member ) || getNamePrefix( 0 ).equals( member )  ) 
                            {
                                String className =  i.substring( 7, i.lastIndexOf( '.' ) ).trim();
                                JavaClass javaClass = classLibrary.getJavaClass( className );
                                JavaField tmpField = javaClass.getFieldByName( member ); 
                                if ( tmpField != null && ( javaClass.isInterface() || tmpField.isStatic() ) )
                                {
                                    field = tmpField;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (field == null) {
            // the field may in any other class, try to resolve it
            for (int i = 0; i < parts.length - 1; ++i) {
                String className = getNamePrefix(i);
                JavaClass javaClass = typeResolver.resolveJavaClass(className);
                if (javaClass != null) {
                    this.fieldIndex++;
                    field = resolveField(javaClass, i + 1, parts.length - 1);
                    if (field != null) {
                        break;
                    }
                }
            }

        }

        return field;
    }

    private JavaClass getDeclaringClass()
    {
        return declaringClass;
    }

    private ClassLibrary getClassLibrary()
    {
        return classLibrary;
    }
}