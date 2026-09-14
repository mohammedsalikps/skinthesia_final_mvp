package com.skinthesia.ai.analysis

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.SkinReport

/** Assembles the human-readable report (screen 22) from a completed assessment. */
class ReportBuilder {

    fun build(assessment: Assessment): SkinReport? {
        val combined = assessment.combined ?: return null
        val camera = assessment.cameraAnalysis
        return SkinReport(
            assessmentId = assessment.id,
            skinPrint = combined.skinPrint,
            strengths = combined.strengths,
            focusAreas = combined.focusAreas,
            measuredValues = combined.sensorSummaries,
            measuredValuesSimulated = combined.sensorSummaries.any { it.source == com.skinthesia.domain.model.DataSource.SENSOR_SIMULATED },
            visualAnalysis = camera?.estimates.orEmpty().sortedByDescending { it.level },
            visualAnalysisSimulated = camera?.isSimulated ?: false,
            insights = combined.insights,
            contextNotes = combined.contextNotes,
        )
    }
}
