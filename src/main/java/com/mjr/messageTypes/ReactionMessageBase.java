package com.mjr.messageTypes;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public abstract class ReactionMessageBase {
	public abstract void onAddReaction(ReactionAddEvent event);

	public abstract void onRemoveReaction(ReactionRemoveEvent event);
}
