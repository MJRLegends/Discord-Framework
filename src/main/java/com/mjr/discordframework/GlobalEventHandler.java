package com.mjr.discordframework;

import discord4j.core.event.domain.message.MessageDeleteEvent;

public class GlobalEventHandler {
	public static void onMessageDelete(MessageDeleteEvent event, DiscordBotBase bot) {
		bot.getReactionMessageManager().removeEmbeddedMessage(event.getMessageId());
	}
}
