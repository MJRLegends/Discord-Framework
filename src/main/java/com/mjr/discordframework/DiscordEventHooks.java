package com.mjr.discordframework;

import com.mjr.discordframework.events.DiscordDebugOutputEvent;
import com.mjr.discordframework.events.DiscordEvent;
import com.mjr.discordframework.managers.DiscordEventListeners;

import discord4j.core.DiscordClient;

public class DiscordEventHooks {

	public static void triggerMessageEvent(DiscordClient client, DiscordBotBase.DiscordMessageType messageType, String message) {
		for (DiscordEvent event : DiscordEventListeners.getEventListeners()) {
			if (DiscordEvent.DiscordEventType.DEBUG.getName().equalsIgnoreCase(event.eventType.getName()))
				((DiscordDebugOutputEvent) event).onEvent(new DiscordDebugOutputEvent(client, messageType, message));
		}
	}

}
