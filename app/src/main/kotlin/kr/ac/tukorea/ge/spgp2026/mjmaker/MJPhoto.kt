package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.graphics.*
import android.util.Log
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Sprite
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.SoundEffects
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.BitmapPool
import kotlin.math.sqrt

data class CollisionCircle(val offsetX: Float, val offsetY: Float, val radius: Float)
class WorldCollisionCircle(var x: Float = 0f, var y: Float = 0f, var radius: Float = 0f)

class MJPhoto(resId: Int, startX: Float, startY: Float, val level: Int = 0) : Sprite(resId) {

    // 댄스 밈 프로파일 정의
    enum class DanceType {
        WIGGLE,
        TOE_STAND,
        PELVIC_THRUST,
        GRAVITY_LEAN
    }

    data class DanceProfile(
        val type: DanceType,
        val duration: Float,
        val cooldown: Float,
        val visualScaleAmount: Float,
        val visualOffsetAmount: Float,
        val collisionScaleInfluence: Float,
        val collisionOffsetInfluence: Float
    )

    companion object {
        data class AlbumInfo(val year: String, val title: String, val colorStart: Int, val colorEnd: Int)

        val ALBUMS = arrayOf(
            AlbumInfo("1972", "Got To Be There", Color.parseColor("#4A148C"), Color.parseColor("#8E24AA")), // Purple
            AlbumInfo("1979", "Off The Wall", Color.parseColor("#0D47A1"), Color.parseColor("#1E88E5")),  // Blue
            AlbumInfo("1982", "Thriller", Color.parseColor("#B71C1C"), Color.parseColor("#E53935")),      // Red
            AlbumInfo("1987", "Bad", Color.parseColor("#212121"), Color.parseColor("#424242")),           // Black/Dark Gray
            AlbumInfo("1991", "Dangerous", Color.parseColor("#006064"), Color.parseColor("#00ACC1")),     // Cyan
            AlbumInfo("1995", "HIStory", Color.parseColor("#1B5E20"), Color.parseColor("#43A047")),       // Green
            AlbumInfo("1997", "Blood On...", Color.parseColor("#E65100"), Color.parseColor("#FB8C00")),    // Orange
            AlbumInfo("2001", "Invincible", Color.parseColor("#4A0E4E"), Color.parseColor("#D81B60"))     // Pink/Magenta
        )

        // 안정성 관련 물리/댄스 조율 상수
        private const val MAX_COLLISION_RADIUS_MULTIPLIER = 1.18f
        private const val MAX_COLLISION_OFFSET_RATIO = 0.35f
        private const val GRAVITY_LEAN_COLLISION_OFFSET_RATIO = 0.22f
        private const val DANCE_COOLDOWN = 1.0f
        private const val SOUND_COOLDOWN = 0.35f

        val DANCE_PROFILES = mapOf(
            DanceType.WIGGLE to DanceProfile(DanceType.WIGGLE, 0.4f, DANCE_COOLDOWN, 0.15f, 0.18f, 0.5f, 0.5f),
            DanceType.TOE_STAND to DanceProfile(DanceType.TOE_STAND, 0.4f, DANCE_COOLDOWN, 0.28f, 0.35f, 0.35f, 0.35f),
            DanceType.PELVIC_THRUST to DanceProfile(DanceType.PELVIC_THRUST, 0.4f, DANCE_COOLDOWN, 0.38f, 0.48f, 0.35f, 0.35f),
            DanceType.GRAVITY_LEAN to DanceProfile(DanceType.GRAVITY_LEAN, 0.4f, DANCE_COOLDOWN, 0.22f, 0.65f, 0.22f, 0.2f)
        )

        private val lpPaint = Paint().apply {
            color = Color.parseColor("#111111")
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        private val lpGroovePaint = Paint().apply {
            color = Color.parseColor("#33FFFFFF")
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        private val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        private val outlinePaint = Paint().apply {
            color = Color.parseColor("#44FFFFFF")
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        private val shinePaint = Paint().apply {
            color = Color.parseColor("#1AFFFFFF")
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        fun getF1ResId(level: Int): Int {
            return when (level) {
                0 -> R.drawable.m_00_f1
                1 -> R.drawable.m_01_f1
                2 -> R.drawable.m_02_f1
                3 -> R.drawable.m_03_f1
                4 -> R.drawable.m_04_f1
                5 -> R.drawable.m_05_f1
                6 -> R.drawable.m_06_f1
                7 -> R.drawable.m_07_f1
                else -> 0
            }
        }

        fun getF2ResId(level: Int): Int {
            return when (level) {
                0 -> R.drawable.m_00_f2
                1 -> R.drawable.m_01_f2
                2 -> R.drawable.m_02_f2
                3 -> R.drawable.m_03_f2
                4 -> R.drawable.m_04_f2
                5 -> R.drawable.m_05_f2
                6 -> R.drawable.m_06_f2
                7 -> R.drawable.m_07_f2
                else -> 0
            }
        }

        private val hitboxCache = mutableMapOf<String, List<CollisionCircle>>()

        fun getAutoHitbox(resId: Int, radius: Float): List<CollisionCircle> {
            val key = "${resId}_${radius}"
            hitboxCache[key]?.let { return it }

            val bmp = BitmapPool.getInstance().get(resId)
            val w = bmp.width
            val h = bmp.height
            val ratio = w.toFloat() / h.toFloat()

            val drawH = radius * 2f
            val drawW = drawH * ratio

            // 6x6 그리드로 나누어 세밀하게 영역 스캔 (원 개수 감소를 통한 연산량 대폭 절감)
            val gridCount = 6
            val cellW = w / gridCount
            val cellH = h / gridCount
            val localCellRadius = (drawH / gridCount) * 0.76f

            val result = mutableListOf<CollisionCircle>()

            for (gy in 0 until gridCount) {
                for (gx in 0 until gridCount) {
                    var opaqueCount = 0
                    val samplePoints = 3 // 3x3 샘플로 속도 향상
                    val stepX = (cellW / samplePoints).coerceAtLeast(1)
                    val stepY = (cellH / samplePoints).coerceAtLeast(1)

                    for (sy in 0 until samplePoints) {
                        for (sx in 0 until samplePoints) {
                            val px = gx * cellW + sx * stepX
                            val py = gy * cellH + sy * stepY
                            if (px < w && py < h) {
                                val pixel = bmp.getPixel(px, py)
                                val alpha = Color.alpha(pixel)
                                if (alpha > 35) { // 불투명 픽셀 판정
                                    opaqueCount++
                                }
                            }
                        }
                    }

                    // 셀 내 샘플 중 일부라도 불투명하면 충돌구로 판정
                    if (opaqueCount >= 2) {
                        val pxCenter = gx * cellW + cellW / 2f
                        val pyCenter = gy * cellH + cellH / 2f

                        val lx = ((pxCenter / w) - 0.5f) * drawW
                        val ly = ((pyCenter / h) - 0.5f) * drawH

                        result.add(CollisionCircle(lx, ly, localCellRadius))
                    }
                }
            }

            if (result.isEmpty()) {
                result.add(CollisionCircle(0f, 0f, radius))
            }

            hitboxCache[key] = result
            return result
        }
    }



    var dx = 0f
    var dy = 0f
    var angle = 0f // 회전 각도 (degree)
    var da = 0f    // 각속도

    var isDropped = false
    var deadTimer = 0f

    // Sleeping (물리 평형 강제 정지) 시스템 변수
    var isSleeping = false
    var sleepTimer = 0f

    fun wakeUp() {
        if (isSleeping) {
            isSleeping = false
            sleepTimer = 0f
        }
    }

    // 댄스 상태변수
    var isAnimating = false
    var animTimer = 0f
    private var danceCooldown = 0f
    private var localSoundCooldown = 0f

    // 비주얼 / 충돌 트랜스폼 값의 분리
    val visualHitOffset = PointF(0f, 0f)
    var visualRadiusMultiplier = 1.0f

    // 멀티서클 충돌체 리스트
    val collisionCircles = mutableListOf<WorldCollisionCircle>()

    // 충돌 캐시 필드 (단일 원 하위 호환성 유지용)
    var cachedCollisionX = 0f
    var cachedCollisionY = 0f
    var cachedCollisionRadius = 0f

    // 외부 노출 충돌 좌표/반경 (캐시값 반환)
    val collisionX: Float get() = cachedCollisionX
    val collisionY: Float get() = cachedCollisionY
    val collisionRadius: Float get() = cachedCollisionRadius

    private val gravity = 1500f
    val radius: Float get() = width / 2

    init {
        x = startX
        y = startY
        val size = 175f + (level * 50f)
        width = size
        height = size
        updateCollisionCache()
    }

    private fun getLocalCollisionCircles(pulse: Float): List<CollisionCircle> {
        val r = radius
        val f1Res = getF1ResId()
        val f2Res = getF2ResId()

        // 불투명 픽셀 윤곽에 따른 자동 충돌원 목록 획득
        val f1Circles = getAutoHitbox(f1Res, r)
        val f2Circles = getAutoHitbox(f2Res, r)

        val result = mutableListOf<CollisionCircle>()
        val maxLen = kotlin.math.max(f1Circles.size, f2Circles.size)
        
        for (i in 0 until maxLen) {
            val c1 = f1Circles.getOrElse(i) { f1Circles.last() }
            val c2 = f2Circles.getOrElse(i) { f2Circles.last() }
            val x = c1.offsetX + (c2.offsetX - c1.offsetX) * pulse
            val y = c1.offsetY + (c2.offsetY - c1.offsetY) * pulse
            val rad = c1.radius + (c2.radius - c1.radius) * pulse
            result.add(CollisionCircle(x, y, rad))
        }
        return result
    }

    private var cachedLocals: List<CollisionCircle>? = null

    // 캐시 필드 강제 업데이트 함수
    fun updateCollisionCache(forceLocalUpdate: Boolean = true) {
        val progress = if (isAnimating) animTimer / getDanceProfile().duration else 0f
        val pulse = if (isAnimating) kotlin.math.sin(progress * Math.PI).toFloat() else 0f
        
        val leanDir = when (level) {
            4 -> if (x > 450f) -1f else 1f
            3 -> if (level % 2 == 0) 1f else -1f
            else -> 1f
        }

        // 1. 비주얼 트랜스폼 캐시 업데이트
        val profile = getDanceProfile()
        if (isAnimating) {
            visualRadiusMultiplier = 1.0f + pulse * profile.visualScaleAmount
            when (profile.type) {
                DanceType.WIGGLE -> {
                    visualHitOffset.x = kotlin.math.sin(progress * Math.PI * 4).toFloat() * (radius * profile.visualOffsetAmount)
                    visualHitOffset.y = 0f
                }
                DanceType.TOE_STAND -> {
                    visualHitOffset.x = 0f
                    visualHitOffset.y = -pulse * (radius * profile.visualOffsetAmount)
                }
                DanceType.PELVIC_THRUST -> {
                    val angleRad = (3.1415927f * 0.25f) * leanDir
                    val push = pulse * (radius * profile.visualOffsetAmount)
                    visualHitOffset.x = push * kotlin.math.cos(angleRad)
                    visualHitOffset.y = push * kotlin.math.sin(angleRad)
                }
                DanceType.GRAVITY_LEAN -> {
                    val leanAmount = pulse * (radius * profile.visualOffsetAmount)
                    visualHitOffset.x = leanAmount * leanDir
                    visualHitOffset.y = leanAmount * 0.3f
                }
            }
        } else {
            visualRadiusMultiplier = 1.0f
            visualHitOffset.set(0f, 0f)
        }

        // 2. 멀티서클 충돌체 생성 및 월드 좌표 변환
        val locals = if (forceLocalUpdate || cachedLocals == null) {
            val l = getLocalCollisionCircles(pulse)
            cachedLocals = l
            l
        } else {
            cachedLocals!!
        }
        
        while (collisionCircles.size < locals.size) {
            collisionCircles.add(WorldCollisionCircle())
        }
        while (collisionCircles.size > locals.size) {
            collisionCircles.removeAt(collisionCircles.lastIndex)
        }

        val cosA = kotlin.math.cos(angle * Math.PI / 180f).toFloat()
        val sinA = kotlin.math.sin(angle * Math.PI / 180f).toFloat()

        for (idx in locals.indices) {
            val local = locals[idx]
            val world = collisionCircles[idx]
            world.x = x + local.offsetX * cosA - local.offsetY * sinA
            world.y = y + local.offsetX * sinA + local.offsetY * cosA
            world.radius = local.radius * 0.95f
        }

        if (collisionCircles.isNotEmpty()) {
            cachedCollisionX = collisionCircles[0].x
            cachedCollisionY = collisionCircles[0].y
            cachedCollisionRadius = collisionCircles[0].radius
        } else {
            cachedCollisionX = x
            cachedCollisionY = y
            cachedCollisionRadius = radius
        }
    }

    fun translateCollisionCircles(tx: Float, ty: Float) {
        x += tx
        y += ty
        for (circle in collisionCircles) {
            circle.x += tx
            circle.y += ty
        }
        if (collisionCircles.isNotEmpty()) {
            cachedCollisionX = collisionCircles[0].x
            cachedCollisionY = collisionCircles[0].y
        } else {
            cachedCollisionX = x
            cachedCollisionY = y
        }
    }

    private fun getDanceProfile(): DanceProfile {
        val type = when (level) {
            0 -> DanceType.WIGGLE
            1 -> DanceType.WIGGLE
            2 -> DanceType.TOE_STAND
            3 -> DanceType.PELVIC_THRUST
            4 -> DanceType.GRAVITY_LEAN
            5 -> DanceType.WIGGLE
            6 -> DanceType.TOE_STAND
            else -> DanceType.PELVIC_THRUST
        }
        return DANCE_PROFILES.getValue(type)
    }

    fun triggerDance(silent: Boolean = false) {
        if (isAnimating || danceCooldown > 0f || !isDropped) return
        
        val profile = getDanceProfile()
        isAnimating = true
        animTimer = profile.duration
        danceCooldown = profile.cooldown
        
        if (silent) return

        // 사운드 쿨다운 검사 후 재생
        if (localSoundCooldown <= 0f) {
            localSoundCooldown = SOUND_COOLDOWN
            try {
                val danceSound = when (level) {
                    in 0..2 -> if (Math.random() < 0.5) R.raw.do2 else R.raw.ddadda
                    in 3..5 -> if (Math.random() < 0.5) R.raw.siu else R.raw.yeahyeah
                    in 6..8 -> if (Math.random() < 0.5) R.raw.dahowda else R.raw.yeeeeah
                    else -> if (Math.random() < 0.5) R.raw.heehee else R.raw.vocal
                }
                SoundEffects.play(danceSound)
            } catch (e: Exception) {
                Log.e("MJPhoto", "Sound play error fallback: ${e.message}")
            }
        }
    }

    override fun update(frameTime: Float) {
        if (!isDropped || isSleeping) {
            dx = 0f
            dy = 0f
            da = 0f
            updateCollisionCache()
            return
        }

        // 쿨다운 차감
        if (danceCooldown > 0f) danceCooldown -= frameTime
        if (localSoundCooldown > 0f) localSoundCooldown -= frameTime

        if (isAnimating) {
            animTimer -= frameTime
            if (animTimer <= 0f) {
                isAnimating = false
            }
        }

        // 중력 적용
        dy += gravity * frameTime
        
        // 위치 및 회전 이동
        x += dx * frameTime
        y += dy * frameTime
        angle += da * frameTime

        // 마찰 및 각속도 감쇄 (감속 비율 상향)
        dx *= 0.980f
        da *= 0.95f 

        // 가로 경계 충돌
        val minX = 50f + radius
        val maxX = 850f - radius
        if (x < minX) {
            x = minX
            dx *= -0.3f
            da += dy * 0.05f 
        } else if (x > maxX) {
            x = maxX
            dx *= -0.3f
            da -= dy * 0.05f
        }

        // 세로 경계 충돌 (바닥)
        val maxY = 1500f - radius
        if (y > maxY) {
            y = maxY
            dy *= -0.1f 
            
            // 바닥 마찰
            da += dx * 0.1f 
            dx *= 0.85f 

            // 정지 임계값 (상향)
            if (kotlin.math.abs(dx) < 8f) dx = 0f
            if (kotlin.math.abs(da) < 3f) da = 0f
        }

        // 업데이트 루프 완료 후 물리 캐시 최신화
        updateCollisionCache()

        // Sleeping 감지 시스템 (속도가 충분히 작으면 정지 타이머 누적)
        val velocityThreshold = 7.0f
        val angularThreshold = 2.0f
        if (kotlin.math.abs(dx) < velocityThreshold && 
            kotlin.math.abs(dy) < velocityThreshold && 
            kotlin.math.abs(da) < angularThreshold) {
            sleepTimer += frameTime
            if (sleepTimer > 0.4f) {
                isSleeping = true
                dx = 0f
                dy = 0f
                da = 0f
            }
        } else {
            sleepTimer = 0f
        }
    }

    private fun getF1ResId(): Int {
        return getF1ResId(level)
    }

    private fun getF2ResId(): Int {
        return getF2ResId(level)
    }

    override fun draw(canvas: Canvas) {
        val f1Res = getF1ResId()
        val f2Res = getF2ResId()

        if (f1Res != 0 && f2Res != 0) {
            val progress = if (isAnimating) animTimer / getDanceProfile().duration else 0f
            val pulse = kotlin.math.sin(progress * Math.PI).toFloat()
            val resId = if (isAnimating && pulse > 0.5f) f2Res else f1Res
            val bmp = BitmapPool.getInstance().get(resId)
            val bmpW = bmp.width.toFloat()
            val bmpH = bmp.height.toFloat()
            val ratio = bmpW / bmpH

            canvas.save()
            canvas.translate(visualHitOffset.x, visualHitOffset.y)
            canvas.scale(visualRadiusMultiplier, visualRadiusMultiplier, x, y)
            canvas.rotate(angle, x, y)

            val drawH = radius * 2
            val drawW = drawH * ratio
            val dst = RectF(x - drawW / 2, y - drawH / 2, x + drawW / 2, y + drawH / 2)
            canvas.drawBitmap(bmp, null, dst, null)

            canvas.restore()
        } else {
            val album = ALBUMS[level.coerceIn(0, ALBUMS.size - 1)]

            // visual (렌더링 전용) 값 기준의 캔버스 찌그러짐 렌더링
            canvas.save()
            canvas.translate(visualHitOffset.x, visualHitOffset.y)
            canvas.scale(visualRadiusMultiplier, visualRadiusMultiplier, x, y)

            // 1. LP판 바디 (검은색 원)
            canvas.drawCircle(x, y, radius, lpPaint)

            // 2. LP판 음각 홈 (동심원 렌더링)
            val numGrooves = (radius / 15f).toInt().coerceAtLeast(3)
            for (i in 1..numGrooves) {
                canvas.drawCircle(x, y, radius * (i.toFloat() / (numGrooves + 1)), lpGroovePaint)
            }

            // 3. 중앙 앨범 라벨 (그라데이션 원)
            val labelRadius = radius * 0.55f
            val labelPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    x, y, labelRadius,
                    album.colorEnd, album.colorStart,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(x, y, labelRadius, labelPaint)

            // 4. LP판 광택 하이라이트 (Glaze)
            canvas.save()
            val path = Path().apply {
                moveTo(x, y)
                arcTo(x - radius, y - radius, x + radius, y + radius, -45f, 30f, false)
                close()
                moveTo(x, y)
                arcTo(x - radius, y - radius, x + radius, y + radius, 135f, 30f, false)
                close()
            }
            canvas.drawPath(path, shinePaint)
            canvas.restore()

            // 5. 텍스트 정보 회전 렌더링
            canvas.save()
            canvas.rotate(angle, x, y)
            
            // 외곽 테두리선
            canvas.drawCircle(x, y, radius, outlinePaint)
            canvas.drawCircle(x, y, labelRadius, outlinePaint)

            // 연도 텍스트 (라벨 상단)
            textPaint.textSize = (labelRadius * 0.4f).coerceAtLeast(18f)
            canvas.drawText(album.year, x, y - (labelRadius * 0.38f), textPaint)

            // 앨범명 텍스트 (라벨 하단)
            textPaint.textSize = (labelRadius * 0.3f).coerceAtLeast(14f)
            val titleText = if (album.title.length > 12) album.title.substring(0, 10) + ".." else album.title
            canvas.drawText(titleText, x, y + (labelRadius * 0.38f), textPaint)

            // 중앙 "MJ" 로고 및 장식용 미세 구멍
            textPaint.textSize = (labelRadius * 0.35f).coerceAtLeast(16f)
            canvas.drawText("MJ", x, y + (labelRadius * 0.05f), textPaint)
            
            // 센터홀 (LP 플레이어 스핀들 꼽는 곳)
            val centerHolePaint = Paint().apply {
                color = Color.parseColor("#222222")
                isAntiAlias = true
            }
            canvas.drawCircle(x, y, labelRadius * 0.12f, centerHolePaint)
            canvas.drawCircle(x, y, labelRadius * 0.12f, outlinePaint)

            canvas.restore() // rotate restore
            canvas.restore() // translate/scale restore
        }

    }

    fun distanceTo(other: MJPhoto): Float {
        val dx = this.collisionX - other.collisionX
        val dy = this.collisionY - other.collisionY
        return sqrt(dx * dx + dy * dy)
    }
}
