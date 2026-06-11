package br.com.redesurftank.havalradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.redesurftank.havalradio.data.Band

@Composable
fun FavoritesPanel(
    favs: List<Int>,
    found: List<Int>,
    band: Band,
    currentFreq: Int?,
    version: String,
    editing: Boolean,
    onToggleEdit: () -> Unit,
    onSaveCurrent: () -> Unit,
    onAbout: () -> Unit,
    onTune: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.35f))
            .border(1.dp, UiKit.Line, RoundedCornerShape(24.dp))
    ) {
        // header
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 20.dp, top = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(width = 5.dp, height = 24.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(12.dp))
            Text("Favoritos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.width(10.dp))
            Text("${favs.size} estações", fontSize = 14.sp, color = UiKit.Muted)
            Spacer(Modifier.weight(1f))
            PillButton(Icons.Filled.Add, "Salvar atual", onClick = onSaveCurrent)
            Spacer(Modifier.width(8.dp))
            PillButton(
                if (editing) Icons.Filled.Check else Icons.Filled.Edit,
                if (editing) "Concluir" else "Editar",
                active = editing,
                onClick = onToggleEdit,
            )
            Spacer(Modifier.width(8.dp))
            VersionButton(version, onAbout)
        }

        // grid de presets
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            gridItems(favs, key = { it }) { freq ->
                PresetCard(
                    freqKHz = freq,
                    band = band,
                    active = freq == currentFreq,
                    editing = editing,
                    slot = favs.indexOf(freq) + 1,
                    onClick = { if (!editing) onTune(freq) },
                    onRemove = { onRemove(freq) },
                )
            }
        }

        // resultados da busca
        if (found.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.width(10.dp))
                    Text("ENCONTRADAS NA BUSCA · toque pra sintonizar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted)
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(found, key = { it }) { freq ->
                        val isFav = favs.contains(freq)
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(13.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, UiKit.Line, RoundedCornerShape(13.dp))
                                .clickable { onTune(freq) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("★", fontSize = 14.sp, color = if (isFav) Color_amber else UiKit.Muted2)
                            Spacer(Modifier.width(8.dp))
                            Text(UiKit.freqLabel(freq, band), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }
}

private val Color_amber = androidx.compose.ui.graphics.Color(0xFFFFB13C)

@Composable
private fun PresetCard(
    freqKHz: Int,
    band: Band,
    active: Boolean,
    editing: Boolean,
    slot: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val logo = UiKit.logoColor(freqKHz, band)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) accent else UiKit.Line,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.verticalGradient(listOf(logo, UiKit.darken(logo)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(UiKit.logoLabel(freqKHz, band), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(UiKit.freqLabel(freqKHz, band), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.width(5.dp))
                Text(band.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        if (editing) {
            Box(
                Modifier.align(Alignment.TopEnd).size(26.dp).clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color(0xFFE0556A))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, "Remover", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(16.dp))
            }
        } else {
            Text("P$slot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UiKit.Muted2, modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}
