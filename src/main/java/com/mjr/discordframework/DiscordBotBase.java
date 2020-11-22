package com.mjr.discordframework;

import java.util.function.Consumer;

import com.mjr.discordframework.handlers.GlobalEventHandler;
import com.mjr.discordframework.reactionMessage.handlers.ReactionMessageEventHandler;
import com.mjr.discordframework.reactionMessage.DiscordReactionMessageManager;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

public abstract class DiscordBotBase {

	public enum DiscordMessageType {
		Info("Info"), Error("Error");

		private final String name;

		DiscordMessageType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private DiscordClient client;
	private EventDispatcher dispatcher;
	private DiscordReactionMessageManager reactionMessageManager;

	/**
	 * Setup Discord Bot instance
	 *
	 * @param token
	 */
	public DiscordBotBase(String token) {
		super();
		if (token.length() == 0) {
			DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Missing Discord oAuth Token!");
			return;
		}
		DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Info, "Discord Bot is starting");
		this.client = connectClient(token);
		this.dispatcher = client.getEventDispatcher();
		this.setReactionMessageManager(new DiscordReactionMessageManager());
		this.dispatcher.on(ReactionAddEvent.class).onErrorContinue((t, o) -> DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Error while processing ReactionAddEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionAddReceivedEvent(o, this));
		this.dispatcher.on(ReactionRemoveEvent.class).onErrorContinue((t, o) -> DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Error while processing ReactionRemoveEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionRemoveReceivedEvent(o, this));
		this.dispatcher.on(MessageDeleteEvent.class).onErrorContinue((t, o) -> DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Error while processing MessageDeleteEvent Error: " + t.getMessage()))
				.subscribe(o -> GlobalEventHandler.onMessageDelete(o, this));
		DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Info, "Discord Bot has been fully started");
		DiscordEventHooks.triggerClientConnectedEvent(this.client, this.dispatcher);
	}

	/**
	 * Used locally to create a client connection
	 *
	 * @param token
	 * @return
	 */
	private DiscordClient connectClient(String token) {
		try {
			DiscordClient temp = new DiscordClientBuilder(token).build();
			temp.login().subscribe();
			return temp;
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Bot was unable to create a connection, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Edit a already sent message
	 *
	 * @param oldMessage
	 * @param content
	 * @return
	 */
	public Message editMessage(Mono<Message> oldMessage, final String content) {
		return editMessage(oldMessage, spec -> spec.setContent(content));
	}

	/**
	 * Edit a already sent message
	 *
	 * @param oldMessage
	 * @param newMessage
	 * @return
	 */
	public Message editMessage(Mono<Message> oldMessage, Consumer<MessageEditSpec> newMessage) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			return oldMessage.block().edit(newMessage).block();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(this.client, DiscordMessageType.Error, "Message could not be edited, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Gets the instance of the bot's DiscordClient object
	 *
	 * @return
	 */
	public DiscordClient getClient() {
		return client;
	}

	/**
	 * Gets the instance of the bot's Dispatcher object
	 *
	 * @return
	 */
	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * Gets the instance of the bot's ReactionMessageManager object
	 *
	 * @return
	 */
	public DiscordReactionMessageManager getReactionMessageManager() {
		return reactionMessageManager;
	}

	/**
	 * Gets the instance of the bot's ReactionMessageManager object
	 *
	 * @param reactionMessageManager
	 */
	public void setReactionMessageManager(DiscordReactionMessageManager reactionMessageManager) {
		this.reactionMessageManager = reactionMessageManager;
	}
}
