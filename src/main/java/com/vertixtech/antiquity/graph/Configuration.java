package com.vertixtech.antiquity.graph;

/**
 * Configuraiton provides a list of properties which defines the behavior of the associated {@link VersionedGraph}.
 */
public class Configuration {
	public final Boolean privateHashEnabled;

	public Configuration(Boolean privateHashEnabled) {
		this.privateHashEnabled = privateHashEnabled;
	}

	public Configuration() {
		privateHashEnabled = true;
	}

	public Boolean getPrivateHashEnabled() {
		return privateHashEnabled;
	}
}
