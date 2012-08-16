package edu.harvard.i2b2.crc.util;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.cache.Cache;
import org.jboss.cache.jmx.CacheJmxWrapperMBean;
import org.jboss.mx.util.MBeanServerLocator;

public class CacheUtil {

	public static Cache getCache() throws MalformedObjectNameException,
			NullPointerException {
		MBeanServer server = MBeanServerLocator.locateJBoss();

		ObjectName on = new ObjectName("jboss.cache:service=Cache");

		CacheJmxWrapperMBean cacheWrapper =

		(CacheJmxWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(
				server, on,

				CacheJmxWrapperMBean.class, false);

		Cache cache = cacheWrapper.getCache();
		return cache;
	}
}
