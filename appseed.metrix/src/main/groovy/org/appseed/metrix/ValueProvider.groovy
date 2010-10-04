package org.appseed.metrix

interface ValueProvider {
	Value newValue(quantity, domain)
}
