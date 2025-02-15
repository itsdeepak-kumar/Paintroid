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
package org.catrobat.paintroid.ui

import com.google.android.material.bottomnavigation.BottomNavigationView
import org.catrobat.paintroid.R
import org.catrobat.paintroid.contract.MainActivityContracts.BottomNavigationAppearance
import org.catrobat.paintroid.tools.ToolType

class BottomNavigationPortrait(private val bottomNavigationView: BottomNavigationView) : BottomNavigationAppearance {
    override fun showCurrentTool(toolType: ToolType) {
        bottomNavigationView.menu.findItem(R.id.action_current_tool).apply {
            if (!this.toString().equals(toolType.name, ignoreCase = true)) {
                setIcon(toolType.drawableResource)
                setTitle(toolType.nameResource)
            }
        }
    }
}
