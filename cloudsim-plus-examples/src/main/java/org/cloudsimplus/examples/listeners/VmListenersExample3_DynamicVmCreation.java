package org.cloudsimplus.examples.listeners;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
import org.cloudsimplus.util.tablebuilder.CloudletsTableBuilderHelper;
import org.cloudsimplus.util.tablebuilder.TextTableBuilder;
import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostToVmEventInfo;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;

/**
 * A simple example showing how to create a data center with 1 host that has
 * capacity to run just one Vm at a time. The example thus shows
 * how to parallelize the execution of multiple VMs.
 * By this way, it creates one Vm a time and enqueue the creation of
 * the next VMs.
 * It uses the new VM listeners to be notified when a host
 * is deallocated for a VM (in this case meaning that the VM has finished
 * executing) in order to place the next VM to run inside the host.
 *
 * @see
 * Vm#setOnHostDeallocationListener(EventListener)
 * @see EventListener
 *
 * @author Manoel Campos da Silva Filho
 */
public class VmListenersExample3_DynamicVmCreation {
    /**
     * Number of Processor Elements (CPU Cores) of each Host.
     */
    private static final int HOST_PES_NUMBER = 2;

    /**
     * Number of Processor Elements (CPU Cores) of each VM and cloudlet.
     */
    private static final int VM_PES_NUMBER = HOST_PES_NUMBER;

    /**
     * Number of Cloudlets to create simultaneously.
     * Other cloudlets will be enqueued.
     */
    private static final int NUMBER_OF_CLOUDLETS = VM_PES_NUMBER;

    /**
     * Total number of VMs to create along the simulation.
     */
    private static final int TOTAL_NUMBER_OF_VMS = 3;

    private final List<Host> hostList;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<DatacenterBroker> brokerList;
    private final Datacenter datacenter;
    private final CloudSim simulation;

    /**
     * Number of VMs that have finished executing all their cloudlets.
     */
    private int numberOfFinishedVms = 0;

    /**
     * The listener object that will be created in order to be notified when
     * a host is deallocated for a VM. The same listener is used for all created VMs.
     * @see #createVmListener()
     */
    private EventListener<HostToVmEventInfo> onHostDeallocationListener;

    /**
     * Starts the example execution, calling the class constructor\
     * to build and run the simulation.
     *
     * @param args command line parameters
     */
    public static void main(String[] args) {
        Log.printFormattedLine("Starting %s ...", VmListenersExample3_DynamicVmCreation.class.getSimpleName());
        new VmListenersExample3_DynamicVmCreation();
        Log.printFormattedLine("%s finished!", VmListenersExample3_DynamicVmCreation.class.getSimpleName());
    }

    /**
     * Default constructor that builds and starts the simulation.
     */
    public VmListenersExample3_DynamicVmCreation() {
        int numberOfUsers = 1; // number of cloud users/customers (brokers)
        simulation = new CloudSim(numberOfUsers);

        this.hostList = new ArrayList<>();
        this.brokerList = new ArrayList<>();
        this.vmList = new ArrayList<>();
        this.cloudletList = new ArrayList<>();
        this.datacenter = createDatacenter();

        createVmListener();

        //Creates the first VM and its cloudlets
        createAndSubmitVmForNewBroker();

        /*Try to create another VM that will fail due to lack of available host PE,
        just to show that VMs have to be executed sequentially.
        See the log messages to check that the VM creation in fact fails.
        The next VMs will be created when the listener is notified about
        the deallocation of a host to a VM (that means the VM has finished executing).*/
        createAndSubmitVmForNewBroker();

        runSimulationAndPrintResults();
    }

    /**
     * Creates the listener object that will be notified when a VM
     * is removed from a host (in this case meaning that it has finished executing
     * its cloudlets). All VMs will use this same listener.
     * @see #createVm(int, DatacenterBroker)
     */
    private void createVmListener() {
        this.onHostDeallocationListener = new EventListener<HostToVmEventInfo>() {
            @Override
            public void update(HostToVmEventInfo evt) {
                numberOfFinishedVms++;
                Log.printFormatted(
                        "\t#EventListener: Vm %d finished running all its cloudlets at time %.0f. ",
                        evt.getVm().getId(), evt.getTime());
                Log.printFormattedLine("VMs finished so far: %d", numberOfFinishedVms);

                createNextVmIfNotReachedMaxNumberOfVms();
            }
        };
    }

    /**
     * After a VM finishes executing, creates another one
     * if the number of created VMs is less than {@link #TOTAL_NUMBER_OF_VMS}.
     * By this way, it allows the sequential execution of several VMs into a host
     * that doesn't have enough PEs to execute them all simultaneously.
     */
    private void createNextVmIfNotReachedMaxNumberOfVms() {
        if(numberOfFinishedVms < TOTAL_NUMBER_OF_VMS) {
            Vm vm = createAndSubmitVmForNewBroker();
            Log.printFormattedLine("\tCreated VM %d at time %.0f", vm.getId(), simulation.clock());
        }
    }

    private void runSimulationAndPrintResults() {
        simulation.start();
        simulation.stop();

        List<Cloudlet> cloudlets;
        String title;
        for(DatacenterBroker broker: brokerList){
            cloudlets = broker.getCloudletsFinishedList();
            title = broker.getName() + (cloudlets.size() > 0 ? "" : " (for the failed VM)");
            new CloudletsTableBuilderHelper(cloudlets)
                    .setPrinter(new TextTableBuilder(title))
                    .build();
        }
    }

    /**
     * Creates cloudlets and submit them to the broker.
     * @param broker The DatacenterBroker that will submit the cloudlets
     * @param vm Vm to run the cloudlets to be created
     *
     * @see #createCloudlet(int, Vm, DatacenterBroker)
     */
    private void createAndSubmitCloudlets(DatacenterBroker broker, Vm vm) {
        int cloudletId = cloudletList.size();
        List<Cloudlet> list = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        for(int i = 0; i < NUMBER_OF_CLOUDLETS; i++){
            Cloudlet cloudlet = createCloudlet(cloudletId++, vm, broker);
            list.add(cloudlet);
        }

        broker.submitCloudletList(list);
        cloudletList.addAll(list);
    }

    /**
     * Creates one Vm and a group of cloudlets to run inside it,
     * and submit the Vm and its cloudlets to a new broker.
     * @return the created VM
     * @see #createVm(int, org.cloudbus.cloudsim.brokers.DatacenterBroker)
     */
    private Vm createAndSubmitVmForNewBroker() {
        List<Vm> list = new ArrayList<>();
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        Vm vm = createVm(this.vmList.size(), broker);
        list.add(vm);

        broker.submitVmList(list);
        this.vmList.addAll(list);

        createAndSubmitCloudlets(broker, vm);
        this.brokerList.add(broker);
        return vm;
    }

    /**
     * Creates a VM with pre-defined configuration.
     *
     * @param id the VM id
     * @param broker the broker that will be submit the VM
     * @return the created VM
     *
     * @see #createVmListener()
     */
    private Vm createVm(int id, DatacenterBroker broker) {
        int mips = 1000;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        Vm vm = new VmSimple(id, mips, VM_PES_NUMBER)
            .setRam(ram).setBw(bw).setSize(size)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared())
            .setBroker(broker)
            .setOnHostDeallocationListener(onHostDeallocationListener);
        return vm;
    }

    /**
     * Creates a cloudlet with pre-defined configuration.
     *
     * @param id Cloudlet id
     * @param vm vm to run the cloudlet
     * @param broker the broker that will submit the cloudlets
     * @return the created cloudlet
     */
    private Cloudlet createCloudlet(int id, Vm vm, DatacenterBroker broker) {
        long fileSize = 300;
        long outputSize = 300;
        long length = 10000; //in number of Million Instructions (MI)
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        Cloudlet cloudlet
                = new CloudletSimple(id, length, pesNumber)
                      .setCloudletFileSize(fileSize)
                      .setCloudletOutputSize(outputSize)
                      .setUtilizationModel(utilizationModel)
                      .setBroker(broker)
                      .setVm(vm);

        return cloudlet;
    }

    /**
     * Creates a switches with pre-defined configuration.
     *
     * @return the created switches
     */
    private Datacenter createDatacenter() {
        Host host = createHost(0);
        hostList.add(host);

        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this switches
        double costPerBw = 0.0; // the cost of using bw in this resource

        DatacenterCharacteristics characteristics =
            new DatacenterCharacteristicsSimple(hostList)
                .setCostPerSecond(cost)
                .setCostPerMem(costPerMem)
                .setCostPerStorage(costPerStorage)
                .setCostPerBw(costPerBw);

        return new DatacenterSimple(simulation, characteristics, new VmAllocationPolicySimple());
    }

    /**
     * Creates a host with pre-defined configuration.
     *
     * @param id The Host id
     * @return the created host
     */
    private Host createHost(int id) {
        List<Pe> peList = new ArrayList<>();
        int mips = 1000;
        for(int i = 0; i < HOST_PES_NUMBER; i++){
            peList.add(new PeSimple(i, new PeProvisionerSimple(mips)));
        }

        long ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage (MB)
        long bw = 10000; //Megabits/s

        return new HostSimple(id, storage, peList)
            .setRamProvisioner(new ResourceProvisionerSimple(new Ram(ram)))
            .setBwProvisioner(new ResourceProvisionerSimple(new Bandwidth(bw)))
            .setVmScheduler(new VmSchedulerSpaceShared());
    }
}
