package com.example.proyectointegrador_moviles_g2.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.proyectointegrador_moviles_g2.data.Clase

// Copia en el celular de lo último que llegó de la API, para verlo sin internet.
// Cada fila lleva el usuario, así un estudiante nunca ve lo guardado de otro.

@Entity(tableName = "clases", primaryKeys = ["usuarioId", "id"])
data class ClaseEntity(
    val usuarioId: String,
    val id: String,
    val ritmo: String,
    val salon: String,
    val profesor: String,
    val horario: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int,
    val profesorId: String,
    val inicioMillis: Long,
    // Cuándo se guardó, para decirle al usuario qué tan reciente es la información.
    val guardadaEn: Long
)

@Entity(tableName = "reservas", primaryKeys = ["usuarioId", "claseId"])
data class ReservaEntity(
    val usuarioId: String,
    val claseId: String,
    val reservaId: String
)

@Dao
interface RitmoDao {

    @Query("SELECT * FROM clases WHERE usuarioId = :usuarioId ORDER BY inicioMillis")
    suspend fun clases(usuarioId: String): List<ClaseEntity>

    @Query("SELECT * FROM reservas WHERE usuarioId = :usuarioId")
    suspend fun reservas(usuarioId: String): List<ReservaEntity>

    @Query("DELETE FROM clases WHERE usuarioId = :usuarioId")
    suspend fun borrarClases(usuarioId: String)

    @Query("DELETE FROM reservas WHERE usuarioId = :usuarioId")
    suspend fun borrarReservas(usuarioId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarClases(clases: List<ClaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarReservas(reservas: List<ReservaEntity>)

    // Reemplaza todo lo guardado de ese usuario en una sola transacción.
    @Transaction
    suspend fun reemplazarClases(usuarioId: String, clases: List<ClaseEntity>) {
        borrarClases(usuarioId)
        guardarClases(clases)
    }

    @Transaction
    suspend fun reemplazarReservas(usuarioId: String, reservas: List<ReservaEntity>) {
        borrarReservas(usuarioId)
        guardarReservas(reservas)
    }
}

@Database(entities = [ClaseEntity::class, ReservaEntity::class], version = 1, exportSchema = false)
abstract class RitmoDatabase : RoomDatabase() {
    abstract fun dao(): RitmoDao

    companion object {
        fun crear(context: Context): RitmoDatabase =
            Room.databaseBuilder(context, RitmoDatabase::class.java, "ritmoapp.db").build()
    }
}

fun Clase.aEntidad(usuarioId: String, guardadaEn: Long) = ClaseEntity(
    usuarioId, id, ritmo, salon, profesor, horario, cupoMaximo, cuposDisponibles, profesorId, inicioMillis, guardadaEn
)

fun ClaseEntity.aClase() = Clase(id, ritmo, salon, profesor, horario, cupoMaximo, cuposDisponibles, profesorId, inicioMillis)
