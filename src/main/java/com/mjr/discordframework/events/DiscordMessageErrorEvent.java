package com.mjr.discordframework.events;

import com.mjr.discordframework.DiscordBotBase;

import discord4j.core.DiscordClient;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DiscordMessageErrorEvent extends DiscordEvent {

	private final String message;
	private final HttpResponseStatus httpResponseStatus;
	private final DiscordClient client;

	public DiscordMessageErrorEvent() {
		super(DiscordEventType.MESSAGE_ERROR);
		this.message = null;
		this.client = null;
		this.httpResponseStatus = null;
	}
	public DiscordMessageErrorEvent(DiscordClient client, String message, HttpResponseStatus httpResponseStatus) {
		super(DiscordEventType.MESSAGE_ERROR);
		this.message = message;
		this.client = client;
		this.httpResponseStatus = httpResponseStatus;
	}

	public void onEvent(DiscordMessageErrorEvent event) {

	}

	public String getMessage() {
		return message;
	}

	public DiscordClient getClient() {
		return client;
	}

	public HttpResponseStatus getHttpResponseStatus() {
		return httpResponseStatus;
	}
}
