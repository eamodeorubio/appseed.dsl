package org.appseed.metrix

import groovy.lang.Immutable;

class Value {
	private ValueDomain domain
	
	def Value(domain) {
		this.domain=domain;
	}
	
	ValueDomain getDomain() {
		domain
	}
}
