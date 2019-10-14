package com.xenoamess.commons.java.net;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;


/**
 * This class defines a factory set for containing several {@code URLStreamHandlerFactory}s.
 * <p>
 * When a protocol comes, it will try to invoke the {@code URLStreamHandlerFactory}s it contains one by one as high
 * priority first and low priority last.
 * <p>
 * It will stop only if it find one {@code URLStreamHandlerFactory} that can return a non-null {@code URLStreamHandler}
 *
 * @see java.net.URL
 * @see java.net.URLStreamHandler
 */
public class URLStreamHandlerFactorySet implements URLStreamHandlerFactory {
    public static final Double DEFAULT_DEFAULT_PRIORITY = 5.0;

    /**
     * If using refuseHandleProtocolSet
     *
     * @see #refuseHandleProtocolSet
     */
    private boolean useRefuseHandleProtocolSet;

    /**
     * key: urlStreamHandlerFactoryName
     * value: URLStreamHandlerFactory
     */
    private final Map<String, URLStreamHandlerFactory> urlStreamHandlerMap = new ConcurrentHashMap<>();

    /**
     * key: urlStreamHandlerFactoryName
     * value: defaultPriority
     */
    private final Map<String, Double> defaultPriorityMap = new ConcurrentHashMap<>();

    /**
     * key: urlStreamHandlerFactoryName
     * value: specialPriority
     */
    private final Map<String, Map<String, Double>> specialPriorityMap =
            new ConcurrentHashMap<>();


    public URLStreamHandlerFactorySet() {
        this(true);
    }

    public URLStreamHandlerFactorySet(boolean useRefuseHandleProtocolSet) {
        this.useRefuseHandleProtocolSet = useRefuseHandleProtocolSet;
    }


    private static final Object streamHandlerLock = new Object();

    /**
     * Create a new URLStreamHandlerFactorySet and set it as URL.factory.
     * Will try to invoke URL.setURLStreamHandlerFactory() first.
     * If success then return the created URLStreamHandlerFactorySet.
     * Otherwise will try to use reflex,
     * and the current URL.factory will be registered in the new created URLStreamHandlerFactorySet with name
     * "original" and default priority 5.0
     *
     * @throws IllegalAccessException
     */
    public static URLStreamHandlerFactorySet wrapURLStreamHandlerFactory() throws IllegalAccessException {
        URLStreamHandlerFactorySet newFactory = new URLStreamHandlerFactorySet();
        try {
            URL.setURLStreamHandlerFactory(newFactory);
        } catch (Error e) {
            synchronized (streamHandlerLock) {
                try {
                    Field factoryField = URL.class.getDeclaredField("factory");
                    factoryField.setAccessible(true);
                    URLStreamHandlerFactory factory = (URLStreamHandlerFactory) factoryField.get(null);
                    newFactory.register("original", factory);
                    factoryField.set(null, newFactory);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException("field URL.factory disappeared.", ex);
                }
            }
        }
        return newFactory;
    }

    /**
     * Creates a new {@code URLStreamHandler} instance with the specified
     * protocol.
     * <p>
     * Will try to use the highest priority urlStreamHandlerFactory of this protocol to generate URLStreamHandler.
     * <p>
     * The rule used in calculating priorities is described at {@code generateSortedURLStreamHandlerFactoryList}.
     *
     * @param protocol the protocol ("{@code ftp}",
     *                 "{@code http}", "{@code nntpp}", etc.).
     * @return a {@code URLStreamHandler} for the secific protocol, or {@code
     * null} if this factory cannot create a handler for the specific
     * protocol
     * @see URLStreamHandler
     * @see #generateSortedURLStreamHandlerFactoryList
     */
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (this.useRefuseHandleProtocolSet && this.refuseHandleProtocolSet.contains(protocol)) {
            return null;
        }

        URLStreamHandler result = null;
        for (ImmutablePair<String, Double> entry : generateSortedURLStreamHandlerFactoryList(protocol)) {
            String urlStreamHandlerFactoryName = entry.getLeft();
            URLStreamHandlerFactory urlStreamHandlerFactory = this.urlStreamHandlerMap.get(urlStreamHandlerFactoryName);
            result = urlStreamHandlerFactory.createURLStreamHandler(protocol);
            if (result != null) {
                return result;
            }
        }

        if (this.useRefuseHandleProtocolSet) {
            this.refuseHandleProtocolSet.add(protocol);
        }
        return null;
    }

    /**
     * First we generate all priority of all urlStreamHandlerFactories registered and of this protocol.
     * A priority of a urlStreamHandlerFactory of a protocol is calculated as such:
     * If there exist specialPriorityMap.get(protocol).get(urlStreamHandlerFactoryName) then it is the priority.
     * Otherwise we use defaultPriorityMap.get(urlStreamHandlerFactoryName) as the priority.
     * <p>
     * Second we sort this list as high priority first and low priority last.
     * <p>
     * return the list.
     *
     * @param protocol
     * @return
     */
    public List<ImmutablePair<String, Double>> generateSortedURLStreamHandlerFactoryList(String protocol) {
        Map<String, Double> priorityMap = new HashMap<>(defaultPriorityMap);
        this.specialPriorityMap.computeIfPresent(protocol, new BiFunction<String, Map<String, Double>, Map<String,
                Double>>() {
            @Override
            public Map<String, Double> apply(String s, Map<String, Double> stringDoubleMap) {
                priorityMap.putAll(stringDoubleMap);
                return stringDoubleMap;
            }
        });

        List<ImmutablePair<String, Double>> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : priorityMap.entrySet()) {
            result.add(new ImmutablePair<>(entry.getKey(), entry.getValue()));
        }
        result.sort(new Comparator<ImmutablePair<String, Double>>() {
            @Override
            public int compare(ImmutablePair<String, Double> o1, ImmutablePair<String, Double> o2) {
                return -Double.compare(o1.getRight(), o2.getRight());
            }
        });
        return result;
    }

    /**
     * register a urlStreamHandlerFactory
     * <p>
     * name : urlStreamHandlerFactory.getClass().getCanonicalName()
     * priority : DEFAULT_DEFAULT_PRIORITY
     *
     * @see #DEFAULT_DEFAULT_PRIORITY
     */
    public void register(URLStreamHandlerFactory urlStreamHandlerFactory) {
        this.register(urlStreamHandlerFactory.getClass().getCanonicalName(), urlStreamHandlerFactory,
                DEFAULT_DEFAULT_PRIORITY);
    }

    /**
     * register a urlStreamHandlerFactory
     * <p>
     * name : urlStreamHandlerFactoryName
     * priority : DEFAULT_DEFAULT_PRIORITY
     *
     * @see #DEFAULT_DEFAULT_PRIORITY
     */
    public void register(String urlStreamHandlerFactoryName, URLStreamHandlerFactory urlStreamHandlerFactory) {
        this.register(urlStreamHandlerFactoryName, urlStreamHandlerFactory, DEFAULT_DEFAULT_PRIORITY);
    }

    /**
     * register a urlStreamHandlerFactory
     * <p>
     * name : urlStreamHandlerFactoryName
     * priority : priority
     */
    public void register(String urlStreamHandlerFactoryName, URLStreamHandlerFactory urlStreamHandlerFactory,
                         double priority) {
        if (urlStreamHandlerMap.containsKey(urlStreamHandlerFactoryName)) {
            throw new IllegalArgumentException("This URLStreamHandlerFactorySet already contains a " +
                    "URLStreamHandlerFactory named " + urlStreamHandlerFactoryName + "." +
                    "URLStreamHandlerFactorySet : " + this + "," +
                    "Existed URLStreamHandlerFactory : " + urlStreamHandlerMap.get(urlStreamHandlerFactoryName) +
                    "You want to register URLStreamHandlerFactory : " + urlStreamHandlerFactory);
        }
        refuseHandleProtocolReset();
        urlStreamHandlerMap.put(urlStreamHandlerFactoryName, urlStreamHandlerFactory);
        defaultPriorityMap.put(urlStreamHandlerFactoryName, priority);
        this.setPriority(urlStreamHandlerFactoryName, priority);
    }

    /**
     * set a specialPriority of this urlStreamHandlerFactory of this protocol.
     *
     * @param urlStreamHandlerFactoryName
     * @param protocol
     * @param priority
     * @see #generateSortedURLStreamHandlerFactoryList
     */
    public void setPriority(String urlStreamHandlerFactoryName, String protocol, double priority) {
        Map<String, Double> urlStreamHandlerMap = specialPriorityMap.putIfAbsent(protocol, new ConcurrentHashMap<>());
        urlStreamHandlerMap.put(urlStreamHandlerFactoryName, priority);
    }

    /**
     * set a defaultPriority of this urlStreamHandlerFactory.
     *
     * @param urlStreamHandlerFactoryName
     * @param priority
     * @see #generateSortedURLStreamHandlerFactoryList
     */
    public void setPriority(String urlStreamHandlerFactoryName, double priority) {
        defaultPriorityMap.put(urlStreamHandlerFactoryName, priority);
    }

    /**
     * get the specialPriority of this urlStreamHandlerFactory.
     *
     * @param urlStreamHandlerFactoryName
     * @param protocol
     * @see #generateSortedURLStreamHandlerFactoryList
     */
    public double getPriority(String urlStreamHandlerFactoryName, String protocol) {
        Map<String, Double> urlStreamHandlerMap = specialPriorityMap.putIfAbsent(protocol, new ConcurrentHashMap<>());
        Double result = urlStreamHandlerMap.get(urlStreamHandlerFactoryName);
        if (result == null) {
            result = getPriority(urlStreamHandlerFactoryName);
        }
        return result;
    }

    /**
     * set the defaultPriority of this urlStreamHandlerFactory.
     *
     * @param urlStreamHandlerFactoryName
     * @see #generateSortedURLStreamHandlerFactoryList
     */
    public double getPriority(String urlStreamHandlerFactoryName) {
        return defaultPriorityMap.get(urlStreamHandlerFactoryName);
    }


    /**
     * refuseHandleProtocolSet.
     * <p>
     * A protocol will be put into this when we failed to find any
     * URLStreamHandlerFactory who can create a handler for this protocol
     * in this URLStreamHandlerFactorySet.
     * <p>
     * this set will be cleared when we register a new
     * <p>
     * this set and mechanism is only active when this.
     */
    private final Set<String> refuseHandleProtocolSet = new ConcurrentSkipListSet<>();

    /**
     * Register this protocol into refuseHandleProtocolSet.
     * <p>
     * This function will be called when we failed to find any
     * URLStreamHandlerFactory who can create a handler for this protocol
     * in this URLStreamHandlerFactorySet.
     *
     * @param protocol the protocol ("{@code ftp}",
     *                 "{@code http}", "{@code nntp}", etc.).
     * @see URLStreamHandler
     */
    private void refuseHandleProtocolRegister(String protocol) {
        if (!useRefuseHandleProtocolSet) {
            return;
        }
        refuseHandleProtocolSet.add(protocol);
    }

    private void refuseHandleProtocolReset() {
        refuseHandleProtocolSet.clear();
    }


    //getters and setters

    /**
     * If using refuseHandleProtocolSet
     *
     * @see #refuseHandleProtocolSet
     */
    public boolean isUseRefuseHandleProtocolSet() {
        return useRefuseHandleProtocolSet;
    }

    /**
     * Set if using refuseHandleProtocolSet
     *
     * @see #refuseHandleProtocolSet
     */
    public void setUseRefuseHandleProtocolSet(boolean useRefuseHandleProtocolSet) {
        this.useRefuseHandleProtocolSet = useRefuseHandleProtocolSet;
    }
}
