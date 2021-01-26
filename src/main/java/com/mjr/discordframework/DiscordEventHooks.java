package com.mjr.discordframework;

import com.mjr.discordframework.events.DiscordConnectedEvent;
import com.mjr.discordframework.events.DiscordDebugOutputEvent;
import com.mjr.discordframework.events.DiscordEvent;
import com.mjr.discordframework.events.DiscordMessageErrorEvent;

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DiscordEventHooks {

	public static void triggerMessageEvent(DiscordClient client, DiscordBotBase.DiscordMessageType messageType, String message) {
		for (DiscordEvent event : DiscordListenerManager.getEventListeners()) {
			if (DiscordEvent.DiscordEventType.DEBUG.getName().equalsIgnoreCase(event.eventType.getName()))
				((DiscordDebugOutputEvent) event).onEvent(new DiscordDebugOutputEvent(client, messageType, message));
		}
	}

	public static void triggerMessageErrorEvent(DiscordClient client, String message, HttpResponseStatus httpResponseStatus) {
		for (DiscordEvent event : DiscordListenerManager.getEventListeners()) {
			if (DiscordEvent.DiscordEventType.MESSAGE_ERROR.getName().equalsIgnoreCase(event.eventType.getName()))
				((DiscordMessageErrorEvent) event).onEvent(new DiscordMessageErrorEvent(client, message, httpResponseStatus));
		}
	}

	public static void triggerClientConnectedEvent(DiscordClient client, EventDispatcher dispatcher) {
		for (DiscordEvent event : DiscordListenerManager.getEventListeners()) {
			if (DiscordEvent.DiscordEventType.CONNECTED.getName().equalsIgnoreCase(event.eventType.getName()))
				((DiscordConnectedEvent) event).onEvent(new DiscordConnectedEvent(client, dispatcher));
		}
	}

}
