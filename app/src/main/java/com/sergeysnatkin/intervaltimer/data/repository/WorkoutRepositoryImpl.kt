package com.sergeysnatkin.intervaltimer.data.repository

import com.sergeysnatkin.intervaltimer.data.AppResult
import com.sergeysnatkin.intervaltimer.data.WorkoutApiService
import com.sergeysnatkin.intervaltimer.data.WorkoutRepository
import com.sergeysnatkin.intervaltimer.data.mapper.toWorkout
import com.sergeysnatkin.intervaltimer.domain.model.Workout
import retrofit2.HttpException
import java.io.IOException

class WorkoutRepositoryImpl(
    private val apiService: WorkoutApiService,
) : WorkoutRepository {

    override suspend fun getWorkout(id: Int): AppResult<Workout> {
        return try {
            val response = apiService.getWorkout(id)
            AppResult.Success(response.toWorkout())
        } catch (httpException: HttpException) {
            AppResult.Error(
                message = httpException.toUserMessage(),
                throwable = httpException,
            )
        } catch (ioException: IOException) {
            AppResult.Error(
                message = "Не удалось загрузить тренировку. Проверьте интернет-соединение и попробуйте еще раз.",
                throwable = ioException,
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                message = throwable.message ?: "Произошла непредвиденная ошибка.",
                throwable = throwable,
            )
        }
    }
}

private fun HttpException.toUserMessage(): String {
    return when (code()) {
        400 -> "Тренировка не найдена. Проверьте ID."
        401, 403 -> "Нет доступа к тренировке."
        404 -> "Тренировка не найдена. Проверьте ID."
        else -> "Не удалось загрузить тренировку. Попробуйте еще раз."
    }
}
