package com.valoser.futacha.shared.media.analysis

import androidx.compose.runtime.Composable
import com.valoser.futacha.shared.media.MediaFeatureGate
import com.valoser.futacha.shared.media.MediaFeaturePermit

internal data class ModelImportRequest(val model: AnalysisModel, val store: ModelStore, val gate: MediaFeatureGate, val permit: MediaFeaturePermit)
internal class ModelImportPicker(val launch: (ModelImportRequest) -> Unit, val cancel: () -> Unit)

