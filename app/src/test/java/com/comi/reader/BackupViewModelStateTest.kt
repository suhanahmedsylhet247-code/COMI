package com.comi.reader

import com.comi.reader.ui.backup.BackupInfo
import com.comi.reader.ui.backup.BackupUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupViewModelStateTest {

    @Test
    fun `BackupUiState defaults`() {
        val state = BackupUiState()
        assertTrue(state.backups.isEmpty())
        assertEquals(0L, state.lastBackupTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `BackupInfo creation`() {
        val info = BackupInfo(
            name = "comi_backup_20250101.json",
            path = "/data/backups/comi_backup_20250101.json",
            size = 50000,
            lastModified = 1704067200000
        )
        assertEquals("comi_backup_20250101.json", info.name)
        assertEquals(50000L, info.size)
    }
}
