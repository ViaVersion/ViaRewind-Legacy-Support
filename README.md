# ViaRewind-Legacy-Support
[![Build Status](https://github.com/ViaVersion/ViaRewind-Legacy-Support/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ViaVersion/ViaRewind/actions)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://viaversion.com/discord)

**Provides additional features for ViaRewind for Paper servers.**

## Information

This is an addon to [ViaRewind](https://github.com/ViaVersion/ViaRewind).

ViaRewind is a multiplatform (Paper, BungeeCord, Velocity and Sponge) plugin and this plugin is for Paper only!

Releases / Dev Builds
-

You can find releases in the following places:

- **Hangar (for our plugins)**: https://hangar.papermc.io/ViaVersion/ViaRewindLegacySupport

Dev builds for **all** of our projects are on our Jenkins server:

- **Jenkins**: https://ci.viaversion.com/view/ViaRewind/

## Installation

Installing this plugin requires nothing more than putting it in your plugins folder. After you started your server once you can also disable/enable certain features in the generated config file. This plugin only works on Spigot. *If you are using ViaRewind on a Proxy, this plugin will not work.*

## Features

- The **Lily Pad** bounding box is being modified. This prevents 1.7.x-1.8.x from glitching if they try to walk over **Lily Pads**.
- The **Ladder** bounding box is being modified. This prevents 1.7.x-1.8.x from glitching if they try to climb **Ladders**.
- If a 1.7.x player opens an **Enchantment Table** this plugin takes Lapis Lazuli from their inventory and puts it in the **Enchantment Table**.
- 1.7.x-1.8.x can interact with **Brewing Stands** to add/remove Blaze Powder to/from them.
- Play block placement and item pickup **sounds** to 1.7.x-1.8.x players.
- Velocity is applied during **Elytra flight** for players below 1.9.
- Players below 1.8 bounce on **Slime Blocks**.
- Makes **AreaEffectClouds** visible for 1.8.x and lower.

Other Links
-
**Maven:** https://repo.viaversion.com/

**Issue tracker:** https://github.com/ViaVersion/ViaRewind-Legacy-Support/issues

**List of contributors:** https://github.com/ViaVersion/ViaRewind-Legacy-Support/graphs/contributors
