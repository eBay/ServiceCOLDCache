/*******************************************************************************
 * Copyright (c) 2016 eBay Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ebayopensource.scc.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    public static Path getRunningJarOrClassFolderPath() {
        Path path = null;
        URL locationUrl = PathUtils.class.getProtectionDomain().getCodeSource().getLocation();
        if(locationUrl != null) {
            try {
                path = Paths.get(locationUrl.toURI());
                if (!Files.isDirectory(path)) {
                    //this means the source code is from an archive, such as a jar
                    path = path.getParent();
                }
            }catch (Exception e) {
                String msg = "Exception threw while trying to get ProxyServer running folder!";
                throw new RuntimeException(msg, e);
            }
        }
        return path;
    }

}
