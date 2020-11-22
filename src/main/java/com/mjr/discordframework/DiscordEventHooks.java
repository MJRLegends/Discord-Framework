package com.mjr.discordframework;

import com.mjr.discordframework.events.DiscordDebugOutputEvent;
import com.mjr.discordframework.events.DiscordEvent;

import discord4j.core.DiscordClient;

public class DiscordEventHooks {

	public static void triggerMessageEvent(DiscordClient client, DiscordBotBase.DiscordMessageType messageType, String message) {
		for (DiscordEvent event : DiscordEventListenerManagers.getEventListeners()) {
			if (DiscordEvent.DiscordEventType.DEBUG.getName().equalsIgnoreCase(event.eventType.getName()))
				((DiscordDebugOutputEvent) event).onEvent(new DiscordDebugOutputEvent(client, messageType, message));
		}
	}

}
