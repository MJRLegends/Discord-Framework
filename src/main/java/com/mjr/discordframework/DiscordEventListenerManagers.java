package com.mjr.discordframework;

import java.util.ArrayList;
import java.util.List;

import com.mjr.discordframework.events.DiscordEvent;

public class DiscordEventListenerManagers {
	private static List<DiscordEvent> listeners = new ArrayList<DiscordEvent>();

	public static void registerEventHandler(DiscordEvent event) {
		listeners.add(event);
	}

	public static List<DiscordEvent> getEventListeners() {
		return listeners;
	}
}
