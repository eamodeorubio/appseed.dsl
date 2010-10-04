package org.appseed.metrix

import groovy.lang.Delegate;


/* IMPLEMENTATION NOTE:
 * Use on the fly properties instead of ExpandoMetaClass for the following reasons:
 * - Seems there are no way to remove a property/getter from an ExpandoMetaClass
 * - Worried about the ThreadLocal issue
 * - If you enable ExpandoMetaClass.enableGlobally() it won't work !
 */
class ValueDomain {
	@Lazy(soft=false) private static final DOMAINS=[:];
	
	static {
		ExpandoMetaClass.disableGlobally()
	}
	
	private static addDomainPropertyToClass(Class over) {
		over.metaClass.getProperty << { String propName ->
			def domain=DOMAINS[propName]
			if(domain&&domain.over.isAssignableFrom(delegate.class))
				return domain
			def metaProp = delegate.metaClass.getMetaProperty(propName)
			if (metaProp) {
				return metaProp.getValue(delegate)
			} else {
				throw new MissingPropertyException(propName, delegate.class)
			}
		}
	}
	
	private boolean enabled;
	private String name;
	private Class over;
	
	def ValueDomain(String name, Class over, enabled=false) {
		super()
		this.name=name;
		this.over=over;
		setEnabled(enabled);
		addDomainPropertyToClass over
	}
	
	def ValueDomain(definition) {
		this(definition.name, definition.over, definition.enabled?:false)
	}
		
	def getName() {
		name
	}
	
	def getOver() {
		over
	}
	
	def getEnabled() {
		enabled
	}
	
	def setEnabled(isEnabled) {
		def newEnabled=isEnabled as Boolean
		if(enabled!=newEnabled) {
			enabled=newEnabled
			if(enabled)
				DOMAINS[name]=this
			else
				DOMAINS.remove(name)
		}
	}
}