package com.example.mycal.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import com.example.mycal.widget.state.CalendarWidgetState
import com.example.mycal.widget.state.CalendarWidgetStateDefinition
import com.example.mycal.widget.ui.CalendarMonthWidget
import org.threeten.bp.LocalDate

class CalendarAppWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<CalendarWidgetState> =
        CalendarWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<CalendarWidgetState>()

            CalendarMonthWidget(
                state = state,
                onPreviousMonth = actionRunCallback<PreviousMonthAction>(),
                onNextMonth = actionRunCallback<NextMonthAction>()
            )
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.mycal.widget.ACTION_UPDATE"
        const val ACTION_PREVIOUS_MONTH = "com.example.mycal.widget.ACTION_PREVIOUS_MONTH"
        const val ACTION_NEXT_MONTH = "com.example.mycal.widget.ACTION_NEXT_MONTH"
    }
}

class PreviousMonthAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, CalendarWidgetStateDefinition, glanceId) { state ->
            val currentDate = LocalDate.of(state.year, state.month, 1)
            val previousMonth = currentDate.minusMonths(1)

            state.copy(
                year = previousMonth.year,
                month = previousMonth.monthValue,
                monthName = previousMonth.month.name,
                isLoading = true
            )
        }

        CalendarAppWidget().update(context, glanceId)

        // Trigger data loading
        val intent = Intent(context, CalendarWidgetReceiver::class.java).apply {
            action = CalendarAppWidget.ACTION_UPDATE_WIDGET
        }
        context.sendBroadcast(intent)
    }
}

class NextMonthAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, CalendarWidgetStateDefinition, glanceId) { state ->
            val currentDate = LocalDate.of(state.year, state.month, 1)
            val nextMonth = currentDate.plusMonths(1)

            state.copy(
                year = nextMonth.year,
                month = nextMonth.monthValue,
                monthName = nextMonth.month.name,
                isLoading = true
            )
        }

        CalendarAppWidget().update(context, glanceId)

        // Trigger data loading
        val intent = Intent(context, CalendarWidgetReceiver::class.java).apply {
            action = CalendarAppWidget.ACTION_UPDATE_WIDGET
        }
        context.sendBroadcast(intent)
    }
}