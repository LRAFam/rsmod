package gg.rsmod.plugins.content.npcs.npcInfo.man

val civilian = arrayOf(Npcs.MAN_3014, Npcs.MAN_3078, Npcs.MAN_3079, Npcs.MAN_3080,
        Npcs.MAN_3081, Npcs.MAN_3082, Npcs.WOMAN_3083, Npcs.MAN_3260, Npcs.MAN_3264, Npcs.MAN_3265,
        Npcs.MAN_3266, Npcs.MAN_3652, Npcs.MAN_6987, Npcs.MAN_6988, Npcs.MAN_6989,
        Npcs.MAN_7281)

civilian.forEach { civilian ->
    on_npc_option(npc = civilian, option = "talk-to") {
        player.queue { chat(this) }
    }

    set_combat_def(civilian) {
        configs {
            attackSpeed = 4
            respawnDelay = 70
        }

        stats {
            hitpoints = 7
            attack = 1
            defence = 1
            strength = 1
            magic = 1
            ranged = 1
        }

        bonuses {
            attackStab = 0
            attackSlash = 0
            attackCrush = 0
            attackMagic = 0
            attackRanged = 0

            defenceStab = -21
            defenceSlash = -21
            defenceCrush = -21
            defenceMagic = -21
            defenceRanged = -21

            attackBonus = 0
            strengthBonus = 0
            rangedStrengthBonus = 0
            magicDamageBonus = 0
        }

        anims {
            death = 836
            block = 425
            attack = 422
        }
    }

    set_drop_table(civilian) {
        droptable {
            dropTableJSON = "man/man.drops.json"
        }
    }
}

suspend fun chat(it: QueueTask) {
    it.chatPlayer("Hello, how's it going?", animation = 567)
    when (world.random(22)) {
        0 -> it.chatNpc("Not too bad thanks.", animation = 567)
        1 -> {
            it.chatNpc("I'm fine, how are you?", animation = 567)
            it.chatPlayer("Very well thank you.", animation = 567)
        }
        2 -> it.chatNpc("I think we need a new king. The one we've got isn't<br>very good.", animation = 589)
        3 -> it.chatNpc("Get out of my way, I'm in a hurry!", animation = 614)
        4 -> it.chatNpc("Do I know you? I'm in a hurry!", animation = 575)
        5 -> it.chatNpc("None of your business.", animation = 614)
        6 -> {
            it.chatNpc("Not too bad, but I'm a little worried about the increase<br>of goblins these days.", animation = 589)
            it.chatPlayer("Don't worry, I'll kill them.", animation = 567)
        }
        7 -> it.chatNpc("I'm busy right now.", animation = 588)
        8 -> {
            it.chatNpc("Who are you?", animation = 575)
            it.chatPlayer("I'm a bold adventurer.", animation = 567)
            it.chatNpc("Ah, a very noble profession.", animation = 567)
        }
        9 -> it.chatNpc("No I don't have any spare change.", animation = 575)
        10 -> {
            it.chatNpc("How can I help you?", animation = 588)
            when (it.options("Do you wish to trade?", "I'm in search of a quest.", "I'm in search of enemies to kill.")) {
                1 -> {
                    it.chatPlayer("Do you wish to trade?", animation = 588)
                    it.chatNpc("No, I have nothing I wish to get rid of. If you want to<br>do some trading, there are plenty of shops and market<br>stalls around though.", animation = 590)
                }
                2 -> {
                    it.chatPlayer("I'm in search of a quest.", animation = 567)
                    it.chatNpc("I'm sorry I can't help you there.", animation = 588)
                }
                3 -> {
                    it.chatPlayer("I'm in search of enemies to kill.", animation = 588)
                    it.chatNpc("I've heard there are many fearsome creatures that<br>dwell under the ground...", animation = 589)
                }
            }
        }
        11 -> it.chatNpc("I'm very well thank you.")
        12 -> it.chatNpc("I'm a little worried - I've heard there's lots of people<br>going about, killing citizens at random.", animation = 589)
        13 -> it.chatNpc("That is classified information.", animation = 588)
        14 -> it.chatNpc("Hello there! Nice weather we've been having.", animation = 567)
        15 -> it.chatNpc("No, I don't want to buy anything!", animation = 614)
        16 -> it.chatNpc("Hello.", animation = 567)
        17 -> {
            it.chatPlayer("I'm in search of enemies to kill.", animation = 588)
            it.chatNpc("I've heard there are many fearsome creatures that<br>dwell under the ground...", animation = 589)
        }
        18 -> {
            it.chatPlayer("Do you wish to trade?", animation = 567)
            it.chatNpc("No, I have nothing I wish to get rid of. If you want to<br>do some trading, there are plenty of shops and market<br>stalls around though.", animation = 590)
        }
        19 -> {
            it.chatPlayer("I'm in search of a quest.", animation = 567)
            it.chatNpc("I'm sorry I can't help you there.", animation = 588)
        }
        20 -> {
            it.chatNpc("Have this flier...", animation = 588)
            it.player.inventory.add(956, 1)
        }
        21 -> {
            val npc = it.player.getInteractingNpc()
            it.chatNpc("Are you asking for a fight?", animation = 614)
            npc.attack(it.player)
        }
    }
}