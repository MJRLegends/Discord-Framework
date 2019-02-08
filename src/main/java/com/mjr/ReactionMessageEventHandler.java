package com.mjr;

import com.mjr.messageTypes.ReactionMessage;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public class ReactionMessageEventHandler {
	public static void onMessageReactionAddReceivedEvent(ReactionAddEvent event, DiscordBotBase bot) {
		if (event.getUser().block().isBot())
			return;
		ReactionMessage msg = bot.getReactionMessageManager().getReactionMessageByMessageID(event.getMessageId());
		if (msg != null) {
			if (msg.getReactions().contains(event.getEmoji().asUnicodeEmoji().get().toString())) {
				msg.removeAllReactionsForUser(event.getMessage(), event.getUserId());
				msg.onAddReaction(event);
			} else
				event.getMessage().block().removeReaction(event.getEmoji(), event.getUserId());
		}
	}

	public static void onMessageReactionRemoveReceivedEvent(ReactionRemoveEvent event, DiscordBotBase bot) {
		if (event.getUser().block().isBot())
			return;
		ReactionMessage msg = bot.getReactionMessageManager().getReactionMessageByMessageID(event.getMessageId());
		if (msg != null) {
			if (msg.getReactions().contains(event.getEmoji().asUnicodeEmoji().get().toString())) {
				msg.removeAllReactionsForUser(event.getMessage(), event.getUserId());
				msg.onRemoveReaction(event);
			} else
				event.getMessage().block().removeReaction(event.getEmoji(), event.getUserId());
		}
	}
}
