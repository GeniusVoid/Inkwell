package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupVersionInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupHistoryModal(
  lastBackupTime: Long,
  backupVersions: List<BackupVersionInfo>,
  isBackupRunning: Boolean,
  backupMessage: String?,
  isGoogleConnected: Boolean,
  googleAccountEmail: String?,
  googleAccountDisplayName: String?,
  onConnectGoogle: () -> Unit,
  onDisconnectGoogle: () -> Unit,
  onBackupNow: () -> Unit,
  onRestoreVersion: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var versionToRestore by remember { mutableStateOf<BackupVersionInfo?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudDone,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Backup & Cloud Storage",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Google Drive & On-Device Snapshots",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Google Drive Account Connection Card
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isGoogleConnected) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
              }
            ),
            border = BorderStroke(
              1.dp,
              if (isGoogleConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
              else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isGoogleConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                    contentDescription = null,
                    tint = if (isGoogleConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Google Drive Account",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                if (isGoogleConnected) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "Connected",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              if (isGoogleConnected) {
                Text(
                  text = "Signed in as ${googleAccountDisplayName ?: ""} (${googleAccountEmail ?: "Active Account"})",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Your backups will upload directly to your private Google Drive app storage.",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                  onClick = onDisconnectGoogle,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("disconnect_google_button")
                ) {
                  Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Disconnect Google Account", style = MaterialTheme.typography.labelSmall)
                }
              } else {
                Text(
                  text = "Authenticate with your Google Account to automatically upload your versioned backups to Google Drive.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                  onClick = onConnectGoogle,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connect_google_drive_button")
                ) {
                  Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Sign in with Google Drive", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        // Manual Backup Action Card
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Create Manual Snapshot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Spacer(modifier = Modifier.height(4.dp))
              val lastTimeText = if (lastBackupTime > 0) {
                SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(lastBackupTime))
              } else {
                "Never"
              }
              Text(
                text = "Last backup: $lastTimeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = onBackupNow,
                enabled = !isBackupRunning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("modal_backup_now_button")
              ) {
                if (isBackupRunning) {
                  CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Creating Backup...")
                } else {
                  Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    if (isGoogleConnected) "Backup Now (Device + Google Drive)"
                    else "Backup Now (On-Device Snapshot)"
                  )
                }
              }

              if (!backupMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = backupMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }
            }
          }
        }

        // Version History Section
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Saved Version Snapshots (Last 5)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (backupVersions.isEmpty()) {
          item {
            Text(
              text = "No saved versions found. Tap 'Backup Now' to create your first safe version snapshot.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }
        } else {
          items(backupVersions) { version ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = version.formattedDate,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${version.bookCount} books · ${version.chapterCount} chapters · ${version.totalWords} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                      Text(
                        text = "Device Storage",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    if (version.isDriveAvailable) {
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                      ) {
                        Text(
                          text = "Google Drive",
                          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }
                }

                OutlinedButton(
                  onClick = { versionToRestore = version },
                  modifier = Modifier.testTag("restore_version_button_${version.fileName}")
                ) {
                  Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Restore", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )

  // Restore Confirmation Dialog
  versionToRestore?.let { version ->
    AlertDialog(
      onDismissRequest = { versionToRestore = null },
      title = { Text("Restore Snapshot?") },
      text = {
        Text(
          "Restoring from snapshot '${version.formattedDate}' will load ${version.bookCount} books and ${version.chapterCount} chapters. Continue?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onRestoreVersion(version.fileName)
            versionToRestore = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_restore_button")
        ) {
          Text("Confirm Restore")
        }
      },
      dismissButton = {
        TextButton(onClick = { versionToRestore = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

