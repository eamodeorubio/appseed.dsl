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
				return domain.valueProvider.newValue(delegate, domain)
			def metaProp = delegate.metaClass.getMetaProperty(propName)
			if (metaProp) {
				return metaProp.getValue(delegate)
			} else {
				throw new MissingPropertyException(propName, delegate.class)
			}
		}
	}
	
	private static ValueDomain checkDomainIsNotRegistered(ValueDomain aDomain) {
		def activeDomain=DOMAINS[aDomain.name]
		if(activeDomain&&activeDomain!=aDomain)
			throw new RuntimeException("Cannot create a new ValueDomain called '${aDomain.name}' over class ${aDomain.over}. There is one already active")
		return aDomain
	}
	
	private boolean enabled;
	private String name;
	private Class over;
	private ValueProvider valueProvider
	
	def ValueDomain(String name, Class over, ValueProvider provider, enabled=false) {
		super()
		this.name=name
		this.over=over
		this.valueProvider=provider
		checkDomainIsNotRegistered this
		setEnabled(enabled);
		addDomainPropertyToClass over
	}
	
	def ValueDomain(definition) {
		this(definition.name, definition.over, definition.valueProvider, definition.enabled?:false)
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
			if(newEnabled) {
				checkDomainIsNotRegistered this
				enabled=true
				DOMAINS[name]=this
			}
			else{
				enabled=false
				DOMAINS.remove(name)
			}
		}
	}
}