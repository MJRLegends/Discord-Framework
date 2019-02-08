package com.mjr;

import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class Utilities {
	/**
	 * @param name
	 * @param guild
	 * @return
	 */
	public Snowflake getRoleIDByName(String name, Mono<Guild> guild) {
		return guild.block().getRoles().filter(role -> role.getName().equalsIgnoreCase(name)).blockFirst().getId();
	}

	/**
	 * @param messageID
	 * @return
	 */
	public Mono<User> getUserByMemberID(DiscordClient client, Snowflake messageID) {
		return client.getUserById(messageID);
	}

	/**
	 * @param member
	 * @return
	 */
	public Mono<User> getUserByMemberID(DiscordClient client, Optional<Member> member) {
		return getUserByMemberID(client, member.get().getId());
	}

	/**
	 * @param member
	 * @return
	 */
	public Mono<User> getUserByMemberID(DiscordClient client, Member member) {
		return getUserByMemberID(client, member.getId());
	}

	/**
	 * @param member
	 * @return
	 */
	public String getUserDisplayNameWithoutEmotes(Optional<Member> member) {
		return getUserDisplayNameWithEmotes(member).replaceAll("[^a-zA-Z0-9_]", "");
	}

	/**
	 * @param member
	 * @return
	 */
	public String getUserDisplayNameWithEmotes(Optional<Member> member) {
		return member.get().getDisplayName();
	}

	/**
	 * @param member
	 * @return
	 */
	public String getUserDisplayNameWithoutEmotes(Member member) {
		return getUserDisplayNameWithEmotes(member).replaceAll("[^a-zA-Z0-9_]", "");
	}

	/**
	 * @param member
	 * @return
	 */
	public String getUserDisplayNameWithEmotes(Member member) {
		return member.getDisplayName();
	}

	/**
	 * @param channelID
	 * @return
	 */
	public Mono<Channel> getChannelByID(DiscordClient client, Snowflake channelID) {
		return client.getChannelById(channelID);
	}

	/**
	 * @param channel
	 * @param messageID
	 * @return
	 */
	public Mono<Message> getMessageByMessageID(DiscordClient client, Mono<Channel> channel, Snowflake messageID) {
		return getMessageByMessageID(client, channel.block().getId(), messageID);
	}

	/**
	 * @param channel
	 * @param messageID
	 * @return
	 */
	public Mono<Message> getMessageByMessageID(DiscordClient client, Snowflake channel, Snowflake messageID) {
		return client.getMessageById(channel, messageID);
	}

	/**
	 * @param channel
	 * @param messageID
	 * @return
	 */
	public Mono<Message> getMessageByMessageID(DiscordClient client, Mono<Channel> channel, Long messageID) {
		return getMessageByMessageID(client, channel.block().getId(), Snowflake.of(messageID));
	}

	/**
	 * @param channel
	 * @param messageID
	 * @return
	 */
	public Mono<Message> getMessageByMessageID(DiscordClient client, Snowflake channel, Long messageID) {
		return client.getMessageById(channel, Snowflake.of(messageID));
	}
}
