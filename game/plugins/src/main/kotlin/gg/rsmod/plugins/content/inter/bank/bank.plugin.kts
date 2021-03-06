package gg.rsmod.plugins.content.inter.bank

import gg.rsmod.game.action.EquipAction
import gg.rsmod.game.model.attr.INTERACTING_ITEM_SLOT
import gg.rsmod.game.model.attr.OTHER_ITEM_SLOT_ATTR
import gg.rsmod.plugins.content.inter.bank.Bank.SELECTED_TAB_VARBIT
import gg.rsmod.plugins.content.inter.bank.Bank.insert
import gg.rsmod.plugins.content.inter.bank.Bank.removePlaceholder
import gg.rsmod.plugins.content.inter.bank.BankTabs.dropToTab
import gg.rsmod.plugins.content.inter.bank.BankTabs.getCurrentTab
import gg.rsmod.plugins.content.inter.bank.BankTabs.insertionPoint
import gg.rsmod.plugins.content.inter.bank.BankTabs.numTabsUnlocked
import gg.rsmod.plugins.content.inter.bank.BankTabs.shiftTabs

on_interface_open(Bank.BANK_INTERFACE_ID) {
    var slotOffset = 0
    for(tab in 1..9){
        val size = player.getVarbit(4170+tab)
        for(slot in slotOffset until slotOffset+size){ // from beginning slot of tab to end
            if(player.bank[slot] == null){
                player.setVarbit(4170+tab, player.getVarbit(4170+tab)-1)
                // check for empty tab shift
                if(player.getVarbit(4170+tab)==0 && tab<=numTabsUnlocked(player))
                    shiftTabs(player, tab)
            }
        }
        slotOffset += size
    }
    player.bank.shift()
}

on_interface_close(Bank.BANK_INTERFACE_ID) {
    player.closeInterface(dest = InterfaceDestination.TAB_AREA)
    player.closeInputDialog()
}

intArrayOf(17, 19).forEachIndexed { index, button ->
    on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = button) {
        player.setVarbit(Bank.REARRANGE_MODE_VARBIT, index)
    }
}

intArrayOf(22, 24).forEachIndexed { index, button ->
    on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = button) {
        player.setVarbit(Bank.WITHDRAW_AS_VARBIT, index)
    }
}

on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = 38) {
    player.toggleVarbit(Bank.ALWAYS_PLACEHOLD_VARBIT)
}

intArrayOf(28, 30, 32, 34, 36).forEach { quantity ->
    on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = quantity) {
        val state = (quantity - 28) / 2
        player.setVarbit(Bank.QUANTITY_VARBIT, state)
    }
}

on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = 42) {
    val from = player.inventory
    val to = player.bank

    var any = false
    for (i in 0 until from.capacity) {
        val item = from[i] ?: continue

        val total = item.amount

        val placeholderSlot = to.removePlaceholder(world, item)
        val deposited = from.transfer(to, item, fromSlot = i, toSlot = placeholderSlot, note = false, unnote = true)?.completed ?: 0
        if (total != deposited) {
            // Was not able to deposit the whole stack of [item].
        }
        if (deposited > 0) {
            any = true
            val curTab = player.getVarbit(SELECTED_TAB_VARBIT)
            if(placeholderSlot==-1 && curTab!=0){
                to.insert(to.getItemIndex(item.id, false), insertionPoint(player, curTab))
                player.setVarbit(4170+curTab, player.getVarbit(4170+curTab)+1)
            }
        }
    }

    if (!any && !from.isEmpty) {
        player.message("Bank full.")
    }
}

on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = 44) {
    val from = player.equipment
    val to = player.bank

    var any = false
    for (i in 0 until from.capacity) {
        val item = from[i] ?: continue

        val total = item.amount

        val placeholderSlot = to.removePlaceholder(world, item)
        val deposited = from.transfer(to, item, fromSlot = i, toSlot = placeholderSlot, note = false, unnote = true)?.completed ?: 0
        if (total != deposited) {
            // Was not able to deposit the whole stack of [item].
        }
        if (deposited > 0) {
            any = true
            val curTab = player.getVarbit(SELECTED_TAB_VARBIT)
            if(placeholderSlot==-1 && curTab!=0){
                to.insert(to.getItemIndex(item.id, false), insertionPoint(player, curTab))
                player.setVarbit(4170+curTab, player.getVarbit(4170+curTab)+1)
            }
            EquipAction.onItemUnequip(player, item.id, i)
        }
    }

    if (!any && !from.isEmpty) {
        player.message("Bank full.")
    }
}

on_button(interfaceId = Bank.INV_INTERFACE_ID, component = Bank.INV_INTERFACE_CHILD) p@ {
    val opt = player.getInteractingOption()
    val slot = player.getInteractingSlot()
    val item = player.inventory[slot] ?: return@p

    if (opt == 10) {
        world.sendExamine(player, item.id, ExamineEntityType.ITEM)
        return@p
    }

    val quantityVarbit = player.getVarbit(Bank.QUANTITY_VARBIT)
    var amount: Int

    when {
        quantityVarbit == 0 -> amount = when (opt) {
            2 -> 1
            4 -> 5
            5 -> 10
            6 -> player.getVarbit(Bank.LAST_X_INPUT)
            7 -> -1 // X
            8 -> 0 // All
            else -> return@p
        }
        opt == 2 -> amount = when (quantityVarbit) {
            1 -> 5
            2 -> 10
            3 -> if (player.getVarbit(Bank.LAST_X_INPUT) == 0) -1 else player.getVarbit(Bank.LAST_X_INPUT)
            4 -> 0 // All
            else -> return@p
        }
        else -> amount = when (opt) {
            3 -> 1
            4 -> 5
            5 -> 10
            6 -> player.getVarbit(Bank.LAST_X_INPUT)
            7 -> -1 // X
            8 -> 0 // All
            else -> return@p
        }
    }

    if (amount == 0) {
        amount = player.inventory.getItemCount(item.id)
    } else if (amount == -1) {
        player.queue(TaskPriority.WEAK) {
            amount = inputInt("How many would you like to bank?")
            if (amount > 0) {
                player.setVarbit(Bank.LAST_X_INPUT, amount)
                Bank.deposit(player, item.id, amount)
            }
        }
        return@p
    }

    Bank.deposit(player, item.id, amount)
}

on_button(interfaceId = Bank.BANK_INTERFACE_ID, component = 13) p@ {
    val opt = player.getInteractingOption()
    val slot = player.getInteractingSlot()
    val item = player.bank[slot] ?: return@p

    if (opt == 10) {
        world.sendExamine(player, item.id, ExamineEntityType.ITEM)
        return@p
    }

    var amount: Int
    var placehold = false

    val quantityVarbit = player.getVarbit(Bank.QUANTITY_VARBIT)
    when {
        quantityVarbit == 0 -> amount = when (opt) {
            1 -> 1
            3 -> 5
            4 -> 10
            5 -> player.getVarbit(Bank.LAST_X_INPUT)
            6 -> -1 // X
            7 -> item.amount
            8 -> item.amount - 1
            9 -> {
                placehold = true
                item.amount
            }
            else -> return@p
        }
        opt == 1 -> amount = when (quantityVarbit) {
            0 -> 1
            1 -> 5
            2 -> 10
            3 -> if (player.getVarbit(Bank.LAST_X_INPUT) == 0) -1 else player.getVarbit(Bank.LAST_X_INPUT)
            4 -> item.amount
            8 -> {
                placehold = true
                item.amount
            }
            else -> return@p
        }
        else -> amount = when (opt) {
            2 -> 1
            3 -> 5
            4 -> 10
            5 -> player.getVarbit(Bank.LAST_X_INPUT)
            6 -> -1 // X
            7 -> item.amount
            8 -> item.amount - 1
            9 -> {
                placehold = true
                item.amount
            }
            else -> return@p
        }
    }

    if (amount == -1) {
        /**
         * Placeholders' "release" option uses the same option
         * as "withdraw-x" would.
         */
        if (item.amount == 0) {
            player.bank[slot] = null
            return@p
        }
        player.queue(TaskPriority.WEAK) {
            amount = inputInt("How many would you like to withdraw?")
            if (amount > 0) {
                player.setVarbit(Bank.LAST_X_INPUT, amount)
                Bank.withdraw(player, item.id, amount, slot, placehold)
            }
        }
        return@p
    }

    amount = Math.max(0, amount)
    if (amount > 0) {
        Bank.withdraw(player, item.id, amount, slot, placehold)
    }
}

/**
 * Swap items in bank inventory interface.
 */
on_component_to_component_item_swap(
        srcInterfaceId = Bank.INV_INTERFACE_ID, srcComponent = Bank.INV_INTERFACE_CHILD,
        dstInterfaceId = Bank.INV_INTERFACE_ID, dstComponent = Bank.INV_INTERFACE_CHILD) {
    val srcSlot = player.attr[INTERACTING_ITEM_SLOT]!!
    val dstSlot = player.attr[OTHER_ITEM_SLOT_ATTR]!!

    val container = player.inventory

    if (srcSlot in 0 until container.capacity && dstSlot in 0 until container.capacity) {
        container.swap(srcSlot, dstSlot)
    }
}

/**
 * Swap items in main bank tab.
 */
on_component_to_component_item_swap(
        srcInterfaceId = Bank.BANK_INTERFACE_ID, srcComponent = Bank.BANK_MAINTAB_COMPONENT,
        dstInterfaceId = Bank.BANK_INTERFACE_ID, dstComponent = Bank.BANK_MAINTAB_COMPONENT) {
    val srcSlot = player.attr[INTERACTING_ITEM_SLOT]!!
    val dstSlot = player.attr[OTHER_ITEM_SLOT_ATTR]!!

    val container = player.bank

    /**
     * Handles the empty box components in the last row of each tab
     * for dropping items into the specified tab's empty space.
     */
    if(dstSlot in 834..843){
        dropToTab(player, dstSlot - 834)
        return@on_component_to_component_item_swap
    }

    if (srcSlot in 0 until container.occupiedSlotCount && dstSlot in 0 until container.occupiedSlotCount) {
        val insertMode = player.getVarbit(Bank.REARRANGE_MODE_VARBIT) == 1
        if (!insertMode) {
            container.swap(srcSlot, dstSlot)
        } else { // insert mode patch for movement between bank tabs and updating varbits
            val curTab = getCurrentTab(player, srcSlot)
            val dstTab = getCurrentTab(player, dstSlot)
            if(dstTab != curTab){
                if((dstTab>curTab && curTab!=0) || dstTab==0)
                    container.insert(srcSlot, dstSlot - 1)
                else
                    container.insert(srcSlot, dstSlot)

                if(dstTab != 0) {player.setVarbit(4170+dstTab, player.getVarbit(4170+dstTab)+1)}
                if(curTab != 0) {
                    player.setVarbit(4170+curTab, player.getVarbit(4170+curTab)-1)
                    if(player.getVarbit(4170+curTab)==0 && curTab<=numTabsUnlocked(player))
                        shiftTabs(player, curTab)
                }
            } else{
                container.insert(srcSlot, dstSlot)
            }
        }
    } else {
        // Sync the container on the client
        container.dirty = true
    }
}