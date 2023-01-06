/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.appmanager.webmodules;

import lombok.Data;

import org.hisp.dhis.appmanager.App;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Torgeir Lorange Ostby
 */

@Data
public class WebModule
{
    @JsonProperty
    private String name;

    @JsonProperty
    private String namespace;

    @JsonProperty
    private String defaultAction;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String icon;

    @JsonProperty
    private String description;

    public WebModule()
    {
    }

    public WebModule( String name )
    {
        this.name = name;
    }

    public WebModule( String name, String namespace )
    {
        this( name, namespace, null );
    }

    public WebModule( String name, String namespace, String defaultAction )
    {
        this.name = name;
        this.namespace = namespace;
        this.defaultAction = defaultAction;
    }

    public static WebModule getModule( App app )
    {
        boolean hasIcon = app.getIcons() != null && app.getIcons().getIcon48() != null;

        String defaultAction = app.getLaunchUrl();

        String icon = hasIcon ? app.getBaseUrl() + "/" + app.getIcons().getIcon48() : null;

        String description = subString( app.getDescription(), 0, 80 );

        WebModule module = new WebModule( app.getShortName(), app.getShortName(), defaultAction );
        module.setIcon( icon );
        module.setDescription( description );
        module.setDisplayName( app.getName() );

        return module;
    }

    public static String subString( String string, int beginIndex, int length )
    {
        if ( string == null )
        {
            return null;
        }

        final int endIndex = beginIndex + length;

        if ( beginIndex >= string.length() )
        {
            return "";
        }

        if ( endIndex > string.length() )
        {
            return string.substring( beginIndex, string.length() );
        }

        return string.substring( beginIndex, endIndex );
    }

    @Override
    public String toString()
    {
        return "[Name: " + name + ", namespace: " + namespace + ", default action: " + defaultAction + "]";
    }
}