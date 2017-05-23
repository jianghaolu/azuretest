/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.test.azure;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.PublicIpAddress;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assume.assumeFalse;

@RunWith(RandomizedRunner.class)
public class AzureTest {

    private static final String CLIENT_ID = "FILL_WITH_YOUR_CLIENT_ID";
    private static final String SECRET = "FILL_WITH_YOUR_SECRET";
    private static final String TENANT = "FILL_WITH_YOUR_TENANT";
    private static final String SUBSCRIPTION_ID = "FILL_WITH_YOUR_SUBSCRIPTION_ID";

    @Test
    public void testConnectWithKeySecret() {
        assumeFalse("Test is skipped unless you use with real credentials",
                CLIENT_ID.startsWith("FILL_WITH_YOUR_") ||
                        SECRET.startsWith("FILL_WITH_YOUR_") ||
                        TENANT.startsWith("FILL_WITH_YOUR_") ||
                        SUBSCRIPTION_ID.startsWith("FILL_WITH_YOUR_"));

        Azure client = Azure.authenticate(new ApplicationTokenCredentials(CLIENT_ID, TENANT, SECRET, AzureEnvironment.AZURE))
                        .withSubscription(SUBSCRIPTION_ID);

        List<AzureVirtualMachine> machines = new ArrayList<>();

        VirtualMachines virtualMachines = client.virtualMachines();
        PagedList<VirtualMachine> vms = virtualMachines.list();

        // We iterate over all VMs and transform them to our internal objects
        for (VirtualMachine vm : vms) {
            machines.add(toAzureVirtualMachine(vm));
        }
    }

    private AzureVirtualMachine toAzureVirtualMachine(VirtualMachine vm) {
        AzureVirtualMachine machine = new AzureVirtualMachine();
        machine.setGroupName(vm.resourceGroupName());
        machine.setName(vm.name());
        if (vm.region() != null) {
            machine.setRegion(vm.region().name());
        }
        machine.setPowerState(AzureVirtualMachine.PowerState.fromAzurePowerState(vm.powerState()));
        PublicIpAddress primaryPublicIpAddress = vm.getPrimaryPublicIpAddress();
        if (primaryPublicIpAddress != null) {
            machine.setPublicIp(primaryPublicIpAddress.ipAddress());
            if (primaryPublicIpAddress.getAssignedNetworkInterfaceIpConfiguration() != null) {
                machine.setPrivateIp(primaryPublicIpAddress.getAssignedNetworkInterfaceIpConfiguration().privateIpAddress());
            }
        }
        return machine;
    }

}
