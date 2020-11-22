package com.mjr.discordframework.events;

import com.mjr.discordframework.DiscordBotBase;

import discord4j.core.DiscordClient;

public class DiscordDebugOutputEvent extends DiscordEvent {

	private final String message;
	private final DiscordClient client;
	private final DiscordBotBase.DiscordMessageType messageType;

	public DiscordDebugOutputEvent() {
		super(DiscordEventType.DEBUG);
		this.message = null;
		this.client = null;
		this.messageType = null;
	}
	public DiscordDebugOutputEvent(DiscordClient client, DiscordBotBase.DiscordMessageType messageType, String message) {
		super(DiscordEventType.DEBUG);
		this.message = message;
		this.client = client;
		this.messageType = messageType;
	}

	public void onEvent(DiscordDebugOutputEvent event) {

	}

	public String getMessage() {
		return message;
	}

	public DiscordClient getClient() {
		return client;
	}

	public DiscordBotBase.DiscordMessageType getMessageType() {
		return messageType;
	}
}
