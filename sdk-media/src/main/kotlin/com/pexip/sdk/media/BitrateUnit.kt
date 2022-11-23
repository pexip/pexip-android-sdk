package com.pexip.sdk.media

/**
 * The list of possible bitrate measurement units, in which a bitrate can be expressed.
 *
 * The smallest time unit is [BPS] and the largest is [MBPS], which corresponds to exactly 1000000 [BPS].
 */
public enum class BitrateUnit {

    /**
     * Bitrate unit representing one bit per second.
     */
    BPS,

    /**
     * Bitrate unit representing one kilobit per second, which is equal to 1000 bits per second.
     */
    KBPS,

    /**
     * Bitrate unit representing one megabit per second, which is equal to 1000 kilobits per second.
     */
    MBPS
}
