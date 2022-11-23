package com.pexip.sdk.media

/**
 * Represents the number of bits that are conveyed or processed per unit of time.
 *
 * A negative bitrate is not possible.
 *
 * The type can store bitrate values up to 2 147 Mbps.
 *
 * To construct bitrate use either the extension function [toBitrate], or the extension properties
 * [bps], [kbps] or [mbps], available on [Int] type.
 *
 * @property bps a bitrate in bits per second
 */
@JvmInline
public value class Bitrate private constructor(public val bps: Int) : Comparable<Bitrate> {

    override fun compareTo(other: Bitrate): Int = bps.compareTo(other.bps)

    public companion object {

        private inline val BitrateUnit.multiplier: Int
            get() = when (this) {
                BitrateUnit.BPS -> 1
                BitrateUnit.KBPS -> 1_000
                BitrateUnit.MBPS -> 1_000_000
            }

        /**
         * Returns a [Bitrate] equal to this [Int] number of bits per second.
         */
        public inline val Int.bps: Bitrate get() = toBitrate(BitrateUnit.BPS)

        /**
         * Returns a [Bitrate] equal to this [Int] number of kilobits per second.
         */
        public inline val Int.kbps: Bitrate get() = toBitrate(BitrateUnit.KBPS)

        /**
         * Returns a [Bitrate] equal to this [Int] number of megabits per second.
         */
        public inline val Int.mbps: Bitrate get() = toBitrate(BitrateUnit.MBPS)

        /**
         * Returns a [Bitrate] equal to this [Int] number of the specified [unit].
         */
        public fun Int.toBitrate(unit: BitrateUnit): Bitrate {
            require(this >= 0) { "Bitrate must be greater than or equal to zero." }
            require(this <= Int.MAX_VALUE / unit.multiplier) { "Bitrate is too high." }
            return Bitrate(this * unit.multiplier)
        }
    }
}
