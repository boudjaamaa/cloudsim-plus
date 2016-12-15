package org.cloudbus.cloudsim.core;

import java.util.*;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.core.predicates.Predicate;
import org.cloudbus.cloudsim.core.predicates.PredicateAny;
import org.cloudbus.cloudsim.core.predicates.PredicateNone;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;

/**
 * An interface to be implemented by a class that manages simulation
 * execution, controlling all the simulation lifecycle.
 *
 * @author Manoel Campos da Silva Filho
 * @see CloudSim
 */
public interface Simulation {
    /**
     * A standard predicate that matches any event.
     */
    PredicateAny SIM_ANY = new PredicateAny();

    /**
     * A standard predicate that does not match any events.
     */
    PredicateNone SIM_NONE = new PredicateNone();


    /**
     * Abruptally terminate the simulation without finishing the processing
     * of entities in the {@link #getEntityList() entities list}, <b>what may give
     * unexpected results</b>.
     * <p><b>Use this method just if you want to abandon the simulation an usually ignore the results.</b></p>
     */
    void abruptallyTerminate();

    /**
     * Adds a new entity to the simulation. Each {@link CloudSimEntity} object
     * register itself when it is instantiated.
     *
     * @param e The new entity
     */
    void addEntity(CloudSimEntity e);

    /**
     * Removes an event from the event queue.
     *
     * @param src Id of entity who scheduled the event.
     * @param p the p
     * @return the sim event
     */
    SimEvent cancel(int src, Predicate p);

    /**
     * Removes all events that match a given predicate from the future event
     * queue returns true if at least one event has been cancelled; false
     * otherwise.
     *
     * @param src Id of entity who scheduled the event.
     * @param p the p
     * @return true, if successful
     */
    boolean cancelAll(int src, Predicate p);

    /**
     * Get the current simulation time.
     *
     * @return the simulation time
     */
    double clock();

    /**
     * Find first deferred event matching a predicate.
     *
     * @param src Id of entity who scheduled the event.
     * @param p the p
     * @return the sim event
     */
    SimEvent findFirstDeferred(int src, Predicate p);

    /**
     * Gets a new copy of initial simulation Calendar.
     *
     * @return a new copy of Calendar object
     * @pre $none
     * @post $none
     */
    Calendar getCalendar();

    /**
     * Gets the entity ID of {@link CloudInformationService}.
     *
     * @return the Entity ID or if it is not found
     * @pre $none
     * @post $result >= -1
     */
    int getCloudInfoServiceEntityId();

    /**
     * Sends a request to Cloud Information Service (CIS) entity to get the list
     * of all Cloud Datacenter IDs.
     *
     * @return a List containing Datacenter IDs
     * @pre $none
     * @post $none
     */
    Set<Integer> getDatacenterIdsList();

    /**
     * Get the entity with a given id.
     *
     * @param id the entity's unique id number
     * @return The entity, or if it could not be found
     */
    SimEntity getEntity(int id);

    /**
     * Get the entity with a given name.
     *
     * @param name The entity's name
     * @return The entity
     */
    SimEntity getEntity(String name);

    /**
     * Get the id of an entity with a given name.
     *
     * @param name The entity's name
     * @return The entity's unique id number
     */
    int getEntityId(String name);

    /**
     * Returns a read-only list of entities created for the simulation.
     *
     * @return
     */
    List<SimEntity> getEntityList();

    /**
     * Gets name of the entity given its entity ID.
     *
     * @param entityId the entity ID
     * @return the Entity name or if this object does not have one
     * @pre entityId > 0
     * @post $none
     */
    String getEntityName(int entityId);

    /**
     * Returns the minimum time between events. Events within shorter periods
     * after the last event are discarded.
     *
     * @return the minimum time between events.
     */
    double getMinTimeBetweenEvents();

    /**
     * Get the current number of entities in the simulation.
     *
     * @return The number of entities
     */
    int getNumEntities();

    /**
     * Gets the {@link EventListener} object that will be notified when any event
     * is processed by CloudSim.
     *
     * @return
     */
    EventListener<SimEvent> getOnEventProcessingListener();

    /**
     * Sets the {@link EventListener} object that will be notified when the simulation is paused.
     * When this Listener is notified, it will receive an {@link EventInfo} informing
     * the time the pause occurred.
     *
     * <p>This object is just information about the event
     * that happened. In fact, it isn't generated an actual {@limk SimEvent} for a pause event
     * because there is not need for that.</p>
     *
     * @param onSimulationPausedListener the event listener to be set
     */
    Simulation setOnSimulationPausedListener(EventListener<EventInfo> onSimulationPausedListener);

    /**
     * Gets the {@link EventListener} object that will be notified when the simulation is paused.
     *
     * @return
     */
    EventListener<EventInfo> getOnSimulationPausedListener();

    /**
     * Sets the {@link EventListener} object that will be notified when any event
     * is processed by CloudSim. When this Listener is notified, it will receive
     * the {@link SimEvent} that was processed.
     *
     * @param onEventProcessingListener the event listener to be set
     */
    Simulation setOnEventProcessingListener(EventListener<SimEvent> onEventProcessingListener);


    /**
     * Pauses an entity for some time.
     *
     * @param src id of entity to be paused
     * @param delay the time period for which the entity will be inactive
     */
    void pauseEntity(int src, double delay);

    /**
     * Holds an entity for some time.
     *
     * @param src id of entity to be held
     * @param delay How many seconds after the current time the entity has to be held
     */
    void holdEntity(int src, long delay);

    /**
     * Checks if the simulation is paused.
     *
     * @return
     */
    boolean isPaused();

    /**
     * Requests the simulation to be paused as soon as possible.
     *
     * @return true if the simulation was paused, false if it was already paused or has finished
     */
    boolean pause();

    /**
     * Requests the simulation to be paused at a given time.
     * The method schedules the pause request and then returns immediately.
     *
     * @param time the time at which the simulation has to be paused
     * @return true if pause request was successfully received (the given time
     * is greater than or equal to the current simulation time), false otherwise.
     */
    boolean pause(double time);

    /**
     * This method is called if one wants to resume the simulation that has
     * previously been paused.
     *
     * @return if the simulation has been restarted or or otherwise.
     */
    boolean resume();

    /**
     * Check if the simulation is still running.
     * Even if the simulation {@link #isPaused() is paused},
     * the method returns true to indicate that the simulation is
     * in fact active yet.
     *
     * This method should be used by
     * entities to check if they should continue executing.
     *
     * @return
     */
    boolean isRunning();

    /**
     * Selects an event matching a predicate.
     *
     * @param src Id of entity who scheduled the event.
     * @param p the p
     * @return the sim event
     */
    SimEvent select(int src, Predicate p);

    /**
     * Sends an event from one entity to another.
     *
     * @param src Id of entity who scheduled the event.
     * @param dest Id of entity that the event will be sent to
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag the tag
     * @param data the data
     */
    void send(int src, int dest, double delay, int tag, Object data);

    /**
     * Used to send an event from one entity to another, with priority in the
     * queue.
     *
     * @param src Id of entity who scheduled the event.
     * @param dest Id of entity that the event will be sent to
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag the tag
     * @param data the data
     */
    void sendFirst(int src, int dest, double delay, int tag, Object data);

    /**
     * Sends an event from one entity to another without delaying
     * the message.
     *
     * @param src Id of entity who scheduled the event.
     * @param dest Id of entity that the event will be sent to
     * @param tag the tag
     * @param data the data
     */
    void sendNow(int src, int dest, int tag, Object data);

    /**
     * Starts the execution of CloudSim simulation and <b>waits for complete
     * execution of all entities</b>, i.e. until all entities threads reach
     * non-RUNNABLE state or there are no more events in the future event queue.
     * <p>
     * <b>Note</b>: This method should be called after all the entities have been setup and added.
     * </p>
     *
     * @return the last clock time
     * @throws RuntimeException When the simulation already run once. If you paused the simulation and wants to resume it,
     * you must use {@link #resume()} instead of {@link #start()}.
     * @pre $none
     * @post $none
     */
    double start() throws RuntimeException;

    /**
     * Forces the termination of the simulation before it ends.
     *
     * @return true if the simulation was running and the termination request was accepted,
     * false if the simulation was not running
     */
    boolean terminate();

    /**
     * Schedules the termination of the simulation for a given time before it has completely finished.
     *
     * @param time the time at which the simulation has to be terminated
     * @return true if successful, false otherwise.
     */
    boolean terminateAt(double time);

    /**
     * Sets an entity's state to be waiting. The predicate used to wait for an
     * event is now passed to Sim_system. Only events that satisfy the predicate
     * will be passed to the entity. This is done to avoid unnecessary context
     * switches.
     *
     * @param src Id of entity who scheduled the event.
     * @param p the p
     */
    void wait(int src, Predicate p);

    /**
     * Removes an entity with and old name from the {@link #getEntitiesByName()} map
     * and adds it again using its new name.
     *
     * @param oldName the name the entity had before
     * @return true if the entity was found and changed into the list, false otherwise
     */
    boolean updateEntityName(final String oldName);

    /**
     * Checks if events for a specific entity are present in the deferred event
     * queue.
     *
     * @param d the d
     * @param p the p
     * @return the int
     */
    int waiting(int d, Predicate p);

    /**
     * Gets the network topology used for Network simulations.
     * @return
     */
    NetworkTopology getNetworkTopology();

    /**
     * Sets the network topology used for Network simulations.
     * @param networkTopology the network topology to set
     */
    void setNetworkTopology(NetworkTopology networkTopology);

    /**
     * Gets a <b>read-only</b> map where each key is the name of an {@link SimEntity} and each value
     * is the actual {@link SimEntity}.
     */
    Map<String, SimEntity> getEntitiesByName();

    /**
     * Gets the number of {@link DatacenterBroker} created. It
     * indicates that the internal {@link CloudCloudSimShutdown} must first
     * wait for all broker entities's END_OF_SIMULATION signal before issuing
     * a terminate signal to other entities.
     *
     * @return
     */
    int getNumberOfUsers();

    /**
     * Adds 1 to the number of {@link DatacenterBroker} created.
     *
     * @return the new value of {@link #getNumberOfUsers()}
     * @see #getNumberOfUsers()
     */
    int incrementNumberOfUsers();

    /**
     * Subtracts 1 from the number of {@link DatacenterBroker} created.
     *
     * @return the new value of {@link #getNumberOfUsers()}
     * @see #getNumberOfUsers()
     */
    int decrementNumberOfUsers();

    Simulation NULL = new Simulation() {
        @Override public void abruptallyTerminate() {}
        @Override public void addEntity(CloudSimEntity e) {}
        @Override public SimEvent cancel(int src, Predicate p) { return SimEvent.NULL; }
        @Override public boolean cancelAll(int src, Predicate p) {
            return false;
        }
        @Override public double clock() {
            return 0;
        }
        @Override public SimEvent findFirstDeferred(int src, Predicate p) { return SimEvent.NULL; }
        @Override public Calendar getCalendar() {
            return Calendar.getInstance();
        }
        @Override public int getCloudInfoServiceEntityId() {
            return 0;
        }
        @Override public Set<Integer> getDatacenterIdsList() {
            return Collections.EMPTY_SET;
        }
        @Override public SimEntity getEntity(int id) { return SimEntity.NULL; }
        @Override public SimEntity getEntity(String name) { return SimEntity.NULL; }
        @Override public int getEntityId(String name) {
            return 0;
        }
        @Override public List<SimEntity> getEntityList() {
            return Collections.EMPTY_LIST;
        }
        @Override public String getEntityName(int entityId) {
            return "";
        }
        @Override public double getMinTimeBetweenEvents() {
            return 0;
        }
        @Override public int getNumEntities() {
            return 0;
        }
        @Override public EventListener<SimEvent> getOnEventProcessingListener() {
            return EventListener.NULL;
        }
        @Override public Simulation setOnSimulationPausedListener(EventListener<EventInfo> onSimulationPausedListener) { return this; }
        @Override public EventListener<EventInfo> getOnSimulationPausedListener() { return EventListener.NULL; }
        @Override public void holdEntity(int src, long delay) {}
        @Override public boolean isPaused() {
            return false;
        }
        @Override public void pauseEntity(int src, double delay) {}
        @Override public boolean pause() {
            return false;
        }
        @Override public boolean pause(double time) {
            return false;
        }
        @Override public boolean resume() {
            return false;
        }
        @Override public boolean isRunning() {
            return false;
        }
        @Override public SimEvent select(int src, Predicate p) {
            return SimEvent.NULL;
        }
        @Override public void send(int src, int dest, double delay, int tag, Object data) {}
        @Override public void sendFirst(int src, int dest, double delay, int tag, Object data) {}
        @Override public void sendNow(int src, int dest, int tag, Object data) {}
        @Override public Simulation setOnEventProcessingListener(EventListener<SimEvent> onEventProcessingListener) { return this; }
        @Override public double start() throws RuntimeException { return 0; }
        @Override public boolean terminate() {
            return false;
        }
        @Override public boolean terminateAt(double time) {
            return false;
        }
        @Override public void wait(int src, Predicate p) {}
        @Override public int waiting(int d, Predicate p) {
            return 0;
        }
        @Override public NetworkTopology getNetworkTopology() { return NetworkTopology.NULL; }
        @Override public void setNetworkTopology(NetworkTopology networkTopology) {}
        @Override public Map<String, SimEntity> getEntitiesByName() { return Collections.emptyMap(); }
        @Override public int getNumberOfUsers() { return 0; }
        @Override public int incrementNumberOfUsers() { return 0; }
        @Override public int decrementNumberOfUsers() { return 0; }
        @Override public boolean updateEntityName(String oldName) {
            return false;
        }
    };


}