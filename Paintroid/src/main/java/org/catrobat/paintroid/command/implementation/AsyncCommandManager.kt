/*
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2021 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.paintroid.command.implementation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.command.CommandManager
import org.catrobat.paintroid.command.CommandManager.CommandListener
import org.catrobat.paintroid.contract.LayerContracts
import org.catrobat.paintroid.model.CommandManagerModel

open class AsyncCommandManager(
    private val commandManager: CommandManager,
    private val layerModel: LayerContracts.Model
) : CommandManager {
    private val commandListeners: MutableList<CommandListener> = ArrayList()
    private var shuttingDown = false
    private var mutex = Mutex()

    override val isBusy: Boolean
        get() = mutex.isLocked

    override val commandManagerModel
        get() = commandManager.commandManagerModel

    override val isUndoAvailable: Boolean
        get() = commandManager.isUndoAvailable

    override val isRedoAvailable: Boolean
        get() = commandManager.isRedoAvailable

    override fun addCommandListener(commandListener: CommandListener) {
        commandListeners.add(commandListener)
    }

    override fun removeCommandListener(commandListener: CommandListener) {
        commandListeners.remove(commandListener)
    }

    override fun addCommand(command: Command?) {
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                if (!shuttingDown) {
                    synchronized(layerModel) { commandManager.addCommand(command) }
                }
                withContext(Dispatchers.Main) {
                    notifyCommandPostExecute()
                }
            }
        }
    }

    override fun loadCommandsCatrobatImage(model: CommandManagerModel?) {
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                if (!shuttingDown) {
                    synchronized(layerModel) { commandManager.loadCommandsCatrobatImage(model) }
                }
                withContext(Dispatchers.Main) {
                    notifyCommandPostExecute()
                }
            }
        }
    }

    override fun undo() {
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                if (!shuttingDown) {
                    synchronized(layerModel) {
                        if (isUndoAvailable) {
                            commandManager.undo()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    notifyCommandPostExecute()
                }
            }
        }
    }

    override fun redo() {
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock {
                if (!shuttingDown) {
                    synchronized(layerModel) {
                        if (isRedoAvailable) {
                            commandManager.redo()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    notifyCommandPostExecute()
                }
            }
        }
    }

    override fun reset() {
        synchronized(layerModel) { commandManager.reset() }
        notifyCommandPostExecute()
    }

    override fun shutdown() {
        shuttingDown = true
    }

    override fun setInitialStateCommand(command: Command) {
        synchronized(layerModel) { commandManager.setInitialStateCommand(command) }
    }

    private fun notifyCommandPostExecute() {
        if (!shuttingDown) {
            for (listener in commandListeners) {
                listener.commandPostExecute()
            }
        }
    }
}
