package org.appseed.metrix

import groovy.lang.ExpandoMetaClass;

class ValueDomain {
	static {
		ExpandoMetaClass.disableGlobally()
	}
	boolean enabled;
	private String name;
	private Class over;
	
	/* IMPLEMENTATION NOTE:
	 * Use on the fly properties instead of ExpandoMetaClass for the following reasons:
	 * - Seems there are no way to remove a property/getter from an ExpandoMetaClass
	 * - Worried about the ThreadLocal issue
	 * - If you enable ExpandoMetaClass.enableGlobally() it won't work !
	 */
	def ValueDomain(String name, Class over, enabled=false) {
		super()
		this.name=name;
		this.over=over;
		this.enabled=enabled;
		def thisDomain=this;
		this.over.metaClass.getProperty << { String propName ->
			if(this.enabled && this.name == propName)
				return this
			def metaProp = delegate.metaClass.getMetaProperty(propName)
			if (metaProp) {
				return metaProp.getValue(delegate)
			} else {
				throw new MissingPropertyException(propName, delegate.class)
			}
		}
	}
	
	def ValueDomain(definition) {
		this(definition.name, definition.over)
		if(definition.enabled)
			enabled=definition.enabled
	}
	
	def getName() {
		name;
	}
	
	def getOver() {
		over;
	}
}