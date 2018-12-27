package com.mjr;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class Discord_Bot {

	public enum MessageType {
		Info("Info"), Error("Error");

		private final String name;

		MessageType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private DiscordClient client;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
	private EventDispatcher dispatcher;

	public Discord_Bot(String token) {
		super();
		if (token.length() == 0) {
			onOutputMessage(MessageType.Error, "Missing Discord oAuth Token!");
			return;
		}
		onOutputMessage(MessageType.Info, "Starting Discord bot");
		this.client = connectClient(token);
		this.dispatcher = client.getEventDispatcher();
		onOutputMessage(MessageType.Info, "Finshed starting Discord bot");
	}

	private DiscordClient connectClient(String token) {
		try {
			DiscordClient temp = new DiscordClientBuilder(token).build();
			temp.login().subscribe();
			return temp;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Bot was unable to create a connection, error: " + e.getMessage());
			return null;
		}
	}

	public Message sendMessageMessageChannelReturnMessageOBJ(Mono<MessageChannel> channel, String message) {
		return sendMessageReturnMessageOBJ(channel.ofType(Channel.class), message);
	}

	public Message sendMessageReturnMessageOBJ(Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
		try {
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}
	
	public Mono<Message> sendMessageMessageChannel(Mono<MessageChannel> channel, String message) {
		return sendMessage(channel.ofType(Channel.class), message);
	}

	public Mono<Message> sendMessage(Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
		try {
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}
	
	public Mono<Message> sendMessageMessageChannel(Mono<MessageChannel> channel,EmbedCreateSpec builder) {
		return sendMessage(channel.ofType(Channel.class), builder);
	}

	private Mono<Message> sendMessage(Mono<Channel> channel, EmbedCreateSpec builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
		try {
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	public void sendDirectMessageToUser(Mono<User> user, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send message to User: " + user.block().getUsername() + " Message: " + message);
		try {
			user.block().getPrivateChannel().block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Private Message could not be sent, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to send message of ```" + message + "```" + " to user " + user.block().getUsername());
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Private Message could not be sent, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to send message of ```" + message + "```" + " to user " + user.block().getUsername());
		}
	}

	public void sendTimedMessageMessageChannel(Mono<MessageChannel> channel, String message, Long delay, TimeUnit timeUnit) {
		sendTimedMessage(channel.ofType(Channel.class), message, delay, timeUnit);
	}

	public void sendTimedMessage(Mono<Channel> channel, EmbedCreateSpec builder, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
		Mono<Message> lastMessage = sendMessage(channel, builder);
		if (lastMessage != null) {
			scheduler.schedule(() -> {
				deleteMessage(channel, lastMessage);
			}, delay, timeUnit);
		}
	}

	public void sendTimedMessage(Mono<Channel> channel, String message, Long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
		Mono<Message> lastMessage = sendMessage(channel, message);
		if (lastMessage != null) {
			scheduler.schedule(() -> {
				deleteMessage(channel, lastMessage);
			}, delay, timeUnit);
		}
	}

	public void sendTimedMessageMessageChannel(Mono<MessageChannel> channel, String message) {
		sendTimedMessage(channel.ofType(Channel.class), message);
	}

	public void sendTimedMessage(Mono<Channel> channel, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
		Mono<Message> lastMessage = sendMessage(channel, message);
		if (lastMessage != null) {
			scheduler.schedule(() -> {
				deleteMessage(channel, lastMessage);
			}, 1L, TimeUnit.MINUTES);
		}
	}

	public void deleteAllMessagesInMessageChannel(Mono<MessageChannel> channel) {
		deleteAllMessagesInChannel(channel.ofType(Channel.class));
	}

	public void deleteAllMessagesInChannel(Mono<Channel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			TextChannel textChannel = channel.ofType(TextChannel.class).block();
			onOutputMessage(MessageType.Info, "Discord: Attempting to run a nuke of all messages on Channel: " + channel.ofType(TextChannel.class).block().getName());
			Flux<Message> messages = textChannel.getMessagesBefore(Snowflake.of(Instant.now()));
			for (Message message : messages.toIterable()) {
				message.delete().doOnError(error -> {
					onOutputMessage(MessageType.Error, "Discord: Channel could not be nuked of messages due to: " + error.getMessage());
					onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
				}).subscribe();
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Channel could not be nuked of messages due to: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	public void deleteMessage(Mono<Message> message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Deleting message with id: " + message.block().getId() + " from " + message.block().getChannel().ofType(TextChannel.class).block().getName());
			message.block().delete().doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.block().getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.block().getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	public void deleteMessage(Mono<Channel> channel, Snowflake messageID) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		deleteMessage(channel, client.getMessageById(channel.block().getId(), messageID));
	}

	public void deleteMessage(Mono<Channel> channel, Mono<Message> message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Deleting message with id: " + message.block().getId() + " from " + channel.ofType(TextChannel.class).block().getName());
			channel.ofType(TextChannel.class).block().getMessageById(message.block().getId()).block().delete().subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	public Mono<Message> editMessage(Mono<Message> oldMessage, MessageEditSpec newMessage) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			oldMessage.block().edit(newMessage);
			oldMessage.subscribe();
			return oldMessage;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be edited, error: " + e.getMessage());
			return null;
		}
	}

	public Snowflake getRoleIDByName(String name, Mono<Guild> guild) {
		return guild.block().getRoles().filter(role -> role.getName().equalsIgnoreCase(name)).blockFirst().getId();
	}

	public Mono<User> getUserByMemberID(Snowflake messageID) {
		return getClient().getUserById(messageID);
	}

	public Mono<User> getUserByMemberID(Optional<Member> member) {
		return getUserByMemberID(member.get().getId());
	}

	public Mono<User> getUserByMemberID(Member member) {
		return getUserByMemberID(member.getId());
	}

	public String getUserDisplayNameWithoutEmotes(Optional<Member> member) {
		return getUserDisplayNameWithEmotes(member).replaceAll("[^a-zA-Z0-9_]", "");
	}

	public String getUserDisplayNameWithEmotes(Optional<Member> member) {
		return member.get().getDisplayName();
	}

	public String getUserDisplayNameWithoutEmotes(Member member) {
		return getUserDisplayNameWithEmotes(member).replaceAll("[^a-zA-Z0-9_]", "");
	}

	public String getUserDisplayNameWithEmotes(Member member) {
		return member.getDisplayName();
	}

	public Mono<Channel> getChannelByID(Snowflake channelID) {
		return getClient().getChannelById(channelID);
	}

	public Mono<Message> getMessageByMessageID(Mono<Channel> channel, Snowflake messageID) {
		return getMessageByMessageID(channel.block().getId(), messageID);
	}

	public Mono<Message> getMessageByMessageID(Snowflake channel, Snowflake messageID) {
		return getClient().getMessageById(channel, messageID);
	}

	public Mono<Message> getMessageByMessageID(Mono<Channel> channel, Long messageID) {
		return getMessageByMessageID(channel.block().getId(), Snowflake.of(messageID));
	}

	public Mono<Message> getMessageByMessageID(Snowflake channel, Long messageID) {
		return getClient().getMessageById(channel, Snowflake.of(messageID));
	}

	public DiscordClient getClient() {
		return client;
	}

	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

	public abstract void onOutputMessage(MessageType type, String message);

	public abstract void setupEvents();
}
