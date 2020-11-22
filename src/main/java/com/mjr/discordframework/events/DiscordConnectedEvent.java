package com.mjr.discordframework.events;

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;

public class DiscordConnectedEvent extends DiscordEvent {

	private final DiscordClient client;
	private final EventDispatcher dispatcher;

	public DiscordConnectedEvent() {
		super(DiscordEventType.CONNECTED);
		this.client = null;
		this.dispatcher = null;
	}

	public DiscordConnectedEvent(DiscordClient client, EventDispatcher dispatcher) {
		super(DiscordEventType.CONNECTED);
		this.client = client;
		this.dispatcher = dispatcher;
	}

	public void onEvent(DiscordConnectedEvent event) {

	}

	public DiscordClient getClient() {
		return client;
	}

	public EventDispatcher getDispatcher() {
		return dispatcher;
	}
}
