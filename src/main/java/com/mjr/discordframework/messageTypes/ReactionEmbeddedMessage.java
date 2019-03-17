package com.mjr.discordframework.messageTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class ReactionEmbeddedMessage extends ReactionMessageBase {

	private List<String> reactions = new ArrayList<String>();
	private List<String> data = new ArrayList<String>();
	private Consumer<EmbedCreateSpec> message;

	public ReactionEmbeddedMessage(Consumer<EmbedCreateSpec> message, List<String> reactions, List<String> data) {
		super();
		this.message = message;
		this.reactions = reactions;
		this.data = data;
	}

	public Consumer<EmbedCreateSpec> getMessage() {
		return message;
	}

	public void setMessage(Consumer<EmbedCreateSpec> message) {
		this.message = message;
	}

	public List<String> getReactions() {
		return reactions;
	}

	public void setReactions(List<String> reactions) {
		this.reactions = reactions;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public void removeAllReactionsForUser(Mono<Message> message, Snowflake user) {
		for (String reactionDefault : this.getReactions())
			message.block().removeReaction(ReactionEmoji.unicode(reactionDefault), user).block();
	}

	@Override
	public void onAddReaction(ReactionAddEvent event) {
	}

	@Override
	public void onRemoveReaction(ReactionRemoveEvent event) {
	}
}
