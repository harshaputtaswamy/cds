/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import java.net.InetAddress

object ClusterUtils {

    /** get the local host name  */
    fun hostname(): String {
        val ip = InetAddress.getLocalHost()
        return ip.hostName
    }

    fun applicationName(): String {
        return BluePrintConstants.APP_NAME
    }

    fun clusterId(): String {
        return System.getenv(BluePrintConstants.PROPERTY_CLUSTER_ID) ?: "cds-cluster"
    }

    fun clusterNodeId(): String {
        return System.getenv(BluePrintConstants.PROPERTY_CLUSTER_NODE_ID) ?: "cds-controller-0"
    }

    fun clusterNodeAddress(): String {
        return System.getenv(BluePrintConstants.PROPERTY_CLUSTER_NODE_ADDRESS)
            ?: clusterNodeId()
    }
}
