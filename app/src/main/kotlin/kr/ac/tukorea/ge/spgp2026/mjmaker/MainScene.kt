package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.GameObject
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.World
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.GameView
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.SoundEffects
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.BitmapPool
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.BGM
import kotlin.math.abs

class MainScene : Scene() {
    enum class Layer {
        Background, Stall, Current, UI
    }

    private var isPaused = false

    override fun onEnter() {
        BGM.play(R.raw.billie_jean_backing_loop)
    }

    override fun onExit() {
        BGM.stop()
    }

    override fun onPause() {
        BGM.pause()
    }

    override fun onResume() {
        BGM.resume()
    }

    companion object {
        private const val DANCE_TRIGGER_SPEED = 140f
        private const val MAX_STATIC_CORRECTION = 28f
        private const val OBJECT_RESTITUTION = 0.25f
        private const val OBJECT_FRICTION = 0.05f
    }

    override val world = World(Layer.entries.toTypedArray())

    private inner class GameBox : GameObject {
        private val paint = Paint().apply {
            color = Color.parseColor("#444444")
            style = Paint.Style.STROKE
            strokeWidth = 8f
            pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
        }
        private val borderPaint = Paint().apply {
            color = Color.parseColor("#333333")
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        private val rect = RectF(50f, 300f, 850f, 1500f)

        override fun update(frameTime: Float) {}
        override fun draw(canvas: Canvas) {
            // 1. 전체 화면 배경 이미지 드로잉
            val isDancing = bgDanceTimer > 0f
            val frameNum = if (isDancing) {
                (bgDanceTimer * 10f).toInt() % 2
            } else {
                0
            }
            
            val bgRes = if (frameNum == 1) R.drawable.bg_stage_f2 else R.drawable.bg_stage_f1
            val bgBmp = BitmapPool.getInstance().get(bgRes)
            
            val screenRect = RectF(0f, 0f, 900f, 1600f)
            canvas.drawBitmap(bgBmp, null, screenRect, null)
            
            // 2. 플레이 영역 어두운 반투명 오버레이 채우기 (가독성 확보)
            val playAreaPaint = Paint().apply {
                color = Color.parseColor("#E6080814")
                style = Paint.Style.FILL
            }
            canvas.drawRect(rect, playAreaPaint)
            
            // 3. 테두리 그리기
            canvas.drawRect(rect, borderPaint)
        }
    }

    private class MergeEffect(val centerX: Float, val centerY: Float, val maxRadius: Float) : GameObject {
        private var radius = 0f
        private val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
        }
        var isDead = false
            private set

        override fun update(frameTime: Float) {
            radius += maxRadius * 5f * frameTime
            paint.strokeWidth = 8f * (1f - radius / maxRadius)
            paint.alpha = ((1f - radius / maxRadius) * 255).toInt().coerceIn(0, 255)
            if (radius >= maxRadius) {
                isDead = true
            }
        }

        override fun draw(canvas: Canvas) {
            if (!isDead) {
                canvas.drawCircle(centerX, centerY, radius, paint)
            }
        }
    }

    private inner class ScoreUI : GameObject {
        private val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 56f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        private val labelPaint = Paint().apply {
            color = Color.parseColor("#888888")
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        private val linePaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
            pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
        }

        override fun update(frameTime: Float) {}
        override fun draw(canvas: Canvas) {
            // 1. 점수판 드로잉 (상단)
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("SCORE", 70f, 70f, labelPaint)
            canvas.drawText(String.format("%05d", score), 70f, 135f, textPaint)

            labelPaint.textAlign = Paint.Align.CENTER
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("BEST", 760f, 70f, labelPaint)
            canvas.drawText(String.format("%05d", bestScore), 760f, 135f, textPaint)

            // 2. 데드라인 시각화 (y = 350f)
            val warning = getAllPhotos().any { it.isDropped && (it.y - it.radius < 350f) }
            if (warning) {
                linePaint.color = if ((System.currentTimeMillis() / 250) % 2 == 0L) Color.RED else Color.TRANSPARENT
            } else {
                linePaint.color = Color.parseColor("#44FFFFFF")
            }
            canvas.drawLine(50f, 350f, 850f, 350f, linePaint)

            // 3. NEXT 미리보기 드로잉
            val nextX = 570f
            val nextY = 90f
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("NEXT", 440f, 95f, labelPaint)
            
            val nextRes = MJPhoto.getF1ResId(nextLevel)
            val miniatureRadius = 35f // 미리보기 크기 확대
            
            if (nextRes != 0) {
                val bmp = BitmapPool.getInstance().get(nextRes)
                val dst = RectF(nextX - miniatureRadius, nextY - miniatureRadius, nextX + miniatureRadius, nextY + miniatureRadius)
                canvas.drawBitmap(bmp, null, dst, null)
            } else {
                val album = MJPhoto.ALBUMS[nextLevel.coerceIn(0, MJPhoto.ALBUMS.size - 1)]
                val miniaturePaint = Paint().apply {
                    color = Color.parseColor("#222222")
                    isAntiAlias = true
                }
                canvas.drawCircle(nextX, nextY, miniatureRadius, miniaturePaint)
                
                val miniatureLabelPaint = Paint().apply {
                    isAntiAlias = true
                    shader = RadialGradient(
                        nextX, nextY, miniatureRadius * 0.5f,
                        album.colorEnd, album.colorStart,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(nextX, nextY, miniatureRadius * 0.5f, miniatureLabelPaint)
                
                miniaturePaint.color = Color.BLACK
                canvas.drawCircle(nextX, nextY, miniatureRadius * 0.1f, miniaturePaint)
            }
        }
    }

    var score = 0
    var bestScore = 0
    private var currentPhoto: MJPhoto? = null
    private var nextLevel = 0
    private var shootCooldown = 0f
    private var isGameOver = false
    private var bgDanceTimer = 0f

    init {
        // SharedPreferences에서 최고 점수 로드
        val prefs = GameView.viewContext.getSharedPreferences("MJ_MAKER_PREFS", Context.MODE_PRIVATE)
        bestScore = prefs.getInt("BEST_SCORE", 0)

        world.add(Layer.Background, GameBox())
        world.add(Layer.UI, ScoreUI())

        nextLevel = (0..2).random()
        spawnNextPhoto()
    }

    private fun spawnNextPhoto() {
        if (isGameOver) return
        val spawnX = 450f
        val spawnY = 250f
        val photo = MJPhoto(android.R.drawable.ic_menu_gallery, spawnX, spawnY, nextLevel)
        photo.isDropped = false
        currentPhoto = photo
        world.add(Layer.Current, photo)

        nextLevel = (0..2).random()
    }

    private fun saveBestScore() {
        val prefs = GameView.viewContext.getSharedPreferences("MJ_MAKER_PREFS", Context.MODE_PRIVATE)
        prefs.edit().putInt("BEST_SCORE", bestScore).apply()
    }

    override fun update(frameTime: Float) {
        if (isGameOver) return
        if (isPaused) return

        super.update(frameTime)

        if (bgDanceTimer > 0f) {
            bgDanceTimer -= frameTime
        }

        // 쿨타임 및 자동 스폰 처리
        if (shootCooldown > 0f) {
            shootCooldown -= frameTime
            if (shootCooldown <= 0f && currentPhoto == null) {
                spawnNextPhoto()
            }
        }

        // 머지 체크 및 충돌 물리 적용
        checkMerge()
        resolveCollisions()
        checkGameOver(frameTime)

        // 이펙트 자가 소멸 처리
        val effects = world.objectsAt(Layer.Stall).filterIsInstance<MergeEffect>()
        for (effect in effects) {
            if (effect.isDead) {
                world.remove(Layer.Stall, effect)
            }
        }
    }

    private fun checkGameOver(frameTime: Float) {
        val photos = getAllPhotos()
        var hasVulnerable = false
        
        for (photo in photos) {
            if (!photo.isDropped) continue
            
            // 데드라인(350f) 위로 넘어선 경우
            if (photo.y - photo.radius < 350f) {
                photo.deadTimer += frameTime
                hasVulnerable = true
                if (photo.deadTimer > 2.5f) {
                    isGameOver = true
                    saveBestScore()
                    Scene.change(ResultScene(score, bestScore))
                    return
                }
            } else {
                photo.deadTimer = 0f
            }
        }
        
        if (!hasVulnerable) {
            for (photo in photos) {
                photo.deadTimer = 0f
            }
        }
    }

    private fun resolveCollisions() {
        val photos = getAllPhotos().filter { it.isDropped }
        
        // 캐시 업데이트 선행 수행
        photos.forEach { it.updateCollisionCache() }

        repeat(2) {
            for (i in photos.indices) {
                for (j in i + 1 until photos.size) {
                    val p1 = photos[i]
                    val p2 = photos[j]

                    // 1. Sleeping vs Sleeping 스킵
                    if (p1.isSleeping && p2.isSleeping) continue

                    // 2. Broad-phase Bounding Circle 검사
                    val dxB = p2.x - p1.x
                    val dyB = p2.y - p1.y
                    val maxBound1 = p1.radius * 1.25f
                    val maxBound2 = p2.radius * 1.25f
                    val minDistBound = maxBound1 + maxBound2
                    if (dxB * dxB + dyB * dyB >= minDistBound * minDistBound) continue

                    // 두 객체의 모든 충돌원 쌍에 대해 검사
                    for (c1 in p1.collisionCircles) {
                        for (c2 in p2.collisionCircles) {
                            val dx = c2.x - c1.x
                            val dy = c2.y - c1.y
                            val minDist = c1.radius + c2.radius
                            
                            // 제곱 거리 비교를 통한 sqrt 연산 조기 스킵
                            val minDistSq = minDist * minDist
                            val distSq = dx * dx + dy * dy
                            if (distSq >= minDistSq) continue

                            // 실제 충돌이 확인된 경우에만 sqrt 호출
                            val dist = kotlin.math.sqrt(distSq)
                            if (dist > 0f) {
                                p1.wakeUp()
                                p2.wakeUp()
                                // 질량 계산 (반경의 제곱인 단면적에 비례)
                                val m1 = p1.radius * p1.radius
                                val m2 = p2.radius * p2.radius
                                val invMass1 = 1f / m1
                                val invMass2 = 1f / m2
                                val invMassSum = invMass1 + invMass2

                                // static correction 보정량 clamp 및 Slop(1.5f 허용 오차) 적용해 무한 진동 방지
                                val slop = 1.5f
                                val overlap = minDist - dist
                                if (overlap > slop) {
                                    val appliedCorrection = (overlap - slop).coerceAtMost(MAX_STATIC_CORRECTION)

                                    // fallback normal (NaN 방지)
                                    val nx = if (dist > 0f) dx / dist else 1f
                                    val ny = if (dist > 0f) dy / dist else 0f

                                    // 질량비에 따른 위치 보정 분배
                                    val p1Corr = appliedCorrection * (invMass1 / invMassSum)
                                    val p2Corr = appliedCorrection * (invMass2 / invMassSum)

                                    val p1Tx = -nx * p1Corr
                                    val p1Ty = -ny * p1Corr
                                    val p2Tx = nx * p2Corr
                                    val p2Ty = ny * p2Corr

                                    // 삼각함수 연산 및 비트맵 스캔 배제하고 단순히 캐시 좌표 평행 이동 적용
                                    p1.translateCollisionCircles(p1Tx, p1Ty)
                                    p2.translateCollisionCircles(p2Tx, p2Ty)
                                }

                                // 접촉 지점(Contact Point) 계산
                                val cDist = dist
                                val cx = if (cDist > 0f) c1.x + (dx / cDist) * c1.radius else c1.x
                                val cy = if (cDist > 0f) c1.y + (dy / cDist) * c1.radius else c1.y

                                // 무게중심(x, y) 기준 지렛대 팔(Lever Arm) 벡터
                                val r1x = cx - p1.x
                                val r1y = cy - p1.y
                                val r2x = cx - p2.x
                                val r2y = cy - p2.y

                                // 관성모멘트의 역수 계산 (I = 0.5 * m * r^2)
                                val invI1 = 2f / (m1 * p1.radius * p1.radius)
                                val invI2 = 2f / (m2 * p2.radius * p2.radius)

                                // 각속도 라디안 변환 (da: degree/s -> w: rad/s)
                                val w1 = p1.da * (Math.PI / 180f).toFloat()
                                val w2 = p2.da * (Math.PI / 180f).toFloat()

                                // 접촉 지점의 실질 속도 (선속도 + 각속도 외적)
                                val vc1x = p1.dx - w1 * r1y
                                val vc1y = p1.dy + w1 * r1x
                                val vc2x = p2.dx - w2 * r2y
                                val vc2y = p2.dy + w2 * r2x

                                // 접촉 지점의 상대 속도
                                val relVelX = vc2x - vc1x
                                val relVelY = vc2y - vc1y
                                val nx = if (dist > 0f) dx / dist else 1f
                                val ny = if (dist > 0f) dy / dist else 0f
                                val velAlongNormal = relVelX * nx + relVelY * ny

                                if (velAlongNormal < 0) {
                                    // 충돌 중일 때 배경 춤추게 발동 (상대속도 50f 이상일 때)
                                    if (velAlongNormal < -50f) {
                                        bgDanceTimer = 0.3f
                                    }

                                    // 고속 충돌 시 댄스 및 사운드 트리거 (DANCE_TRIGGER_SPEED = 140f)
                                    if (velAlongNormal < -DANCE_TRIGGER_SPEED) {
                                        if (p1.level >= p2.level) {
                                            p1.triggerDance(false)
                                            p2.triggerDance(true)
                                        } else {
                                            p1.triggerDance(true)
                                            p2.triggerDance(false)
                                        }
                                    }

                                    // 법선 방향 토크 기여도 (r x n)
                                    val rn1 = r1x * ny - r1y * nx
                                    val rn2 = r2x * ny - r2y * nx

                                    // 회전력을 포함한 충격량 분모
                                    val kNormal = invMass1 + invMass2 + (rn1 * rn1) * invI1 + (rn2 * rn2) * invI2

                                    // 충돌 임펄스 계산 (속도가 아주 느릴 때, 즉 -45f보다 클 때는 반발력을 0으로 해 평형 도달 유도)
                                    val restitution = if (velAlongNormal > -45f) 0f else OBJECT_RESTITUTION
                                    val impulseJ = -(1f + restitution) * velAlongNormal / kNormal

                                    val impulseX = impulseJ * nx
                                    val impulseY = impulseJ * ny

                                    // 선속도 변화 적용
                                    p1.dx -= impulseX * invMass1
                                    p1.dy -= impulseY * invMass1
                                    p2.dx += impulseX * invMass2
                                    p2.dy += impulseY * invMass2

                                    // 법선 임펄스로 인한 각속도 변화 (dw = (r x J) / I) 및 degree 변환
                                    val dw1 = -(r1x * impulseY - r1y * impulseX) * invI1
                                    val dw2 = (r2x * impulseY - r2y * impulseX) * invI2
                                    p1.da += dw1 * (180f / Math.PI).toFloat()
                                    p2.da += dw2 * (180f / Math.PI).toFloat()

                                    // ----------------------------------------
                                    // 접선 마찰 임펄스 계산 (마찰로 인한 회전 연동)
                                    // ----------------------------------------
                                    val tx = -ny
                                    val ty = nx
                                    val velAlongTangent = relVelX * tx + relVelY * ty

                                    val rt1 = r1x * ty - r1y * tx
                                    val rt2 = r2x * ty - r2y * tx
                                    val kTangent = invMass1 + invMass2 + (rt1 * rt1) * invI1 + (rt2 * rt2) * invI2

                                    val jt = -velAlongTangent * OBJECT_FRICTION / kTangent

                                    val tangentImpulseX = jt * tx
                                    val tangentImpulseY = jt * ty

                                    p1.dx -= tangentImpulseX * invMass1
                                    p1.dy -= tangentImpulseY * invMass1
                                    p2.dx += tangentImpulseX * invMass2
                                    p2.dy += tangentImpulseX * invMass2

                                    val dwt1 = -(r1x * tangentImpulseY - r1y * tangentImpulseX) * invI1
                                    val dwt2 = (r2x * tangentImpulseY - r2y * tangentImpulseX) * invI2
                                    p1.da += dwt1 * (180f / Math.PI).toFloat()
                                    p2.da += dwt2 * (180f / Math.PI).toFloat()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkMerge() {
        val photos = getAllPhotos().filter { it.isDropped }
        val toRemove = mutableSetOf<MJPhoto>()
        var toAdd: MJPhoto? = null

        for (i in photos.indices) {
            val p1 = photos[i]
            if (toRemove.contains(p1)) continue

            for (j in i + 1 until photos.size) {
                val p2 = photos[j]
                if (toRemove.contains(p2)) continue

                if (p1.level == p2.level) {
                    // Broad-phase로 대략적인 필터링
                    val mDx = p2.x - p1.x
                    val mDy = p2.y - p1.y
                    val maxBound = (p1.radius + p2.radius) * 1.3f
                    if (mDx * mDx + mDy * mDy >= maxBound * maxBound) continue

                    // 자식 충돌구 중 하나라도 접촉했는지 실제 정밀 검사
                    var isTouching = false
                    for (c1 in p1.collisionCircles) {
                        for (c2 in p2.collisionCircles) {
                            val cx = c2.x - c1.x
                            val cy = c2.y - c1.y
                            val minDist = c1.radius + c2.radius
                            if (cx * cx + cy * cy < minDist * minDist) {
                                isTouching = true
                                break
                            }
                        }
                        if (isTouching) break
                    }

                    if (isTouching) {
                        toRemove.add(p1)
                        toRemove.add(p2)
                        
                        val newLevel = p1.level + 1
                        val spawnX = (p1.x + p2.x) / 2
                        val spawnY = (p1.y + p2.y) / 2
                        
                        // 최고 레벨(7)인 경우 더이상 머지되지 않고 사라지며 대량 보너스
                        if (p1.level >= 7) {
                            score += 500
                        } else {
                            toAdd = MJPhoto(
                                android.R.drawable.ic_menu_gallery,
                                spawnX,
                                spawnY,
                                newLevel
                            ).apply {
                                isDropped = true
                                // 머지 시 가볍게 사방으로 튕겨나가는 쾌감을 주기 위한 초속 부여
                                dx = (Math.random().toFloat() - 0.5f) * 100f
                                dy = -100f
                            }
                            score += (p1.level + 1) * 10
                        }

                        if (score > bestScore) {
                            bestScore = score
                        }

                        // 머지 사운드 선택 및 재생
                        val mergeSound = when (p1.level) {
                            in 0..2 -> if (Math.random() < 0.5) R.raw.do2 else R.raw.ddadda
                            in 3..5 -> if (Math.random() < 0.5) R.raw.siu else R.raw.yeahyeah
                            in 6..8 -> if (Math.random() < 0.5) R.raw.dahowda else R.raw.yeeeeah
                            else -> if (Math.random() < 0.5) R.raw.heehee else R.raw.vocal
                        }
                        SoundEffects.play(mergeSound)

                        // 배경 댄스 연출 발동 (0.5초)
                        bgDanceTimer = 0.5f

                        // 머지 서클 이펙트 생성
                        world.add(Layer.Stall, MergeEffect(spawnX, spawnY, (p1.radius + p2.radius)))
                        break
                    }
                }
            }
            if (toRemove.isNotEmpty()) break
        }

        toRemove.forEach { 
            world.remove(Layer.Stall, it)
            world.remove(Layer.Current, it)
        }
        toAdd?.let { world.add(Layer.Stall, it) }
    }

    private fun getAllPhotos(): List<MJPhoto> {
        return world.objectsAt(Layer.Stall).filterIsInstance<MJPhoto>() +
               world.objectsAt(Layer.Current).filterIsInstance<MJPhoto>()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isGameOver) return false

        // 1. 일시정지 상태일 때 터치 처리
        if (isPaused) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val tx = event.x
                val ty = event.y

                // 게임 재개 (RESUME): X: 200f ~ 700f, Y: 550f ~ 650f
                if (tx in 200f..700f && ty in 550f..650f) {
                    isPaused = false
                    BGM.resume()
                    SoundEffects.play(R.raw.heehee) // 피드백 사운드
                    return true
                }
                
                // 로비로 가기 (LOBBY): X: 200f ~ 700f, Y: 700f ~ 800f
                if (tx in 200f..700f && ty in 700f..800f) {
                    BGM.stop()
                    SoundEffects.play(R.raw.heehee)
                    Scene.change(TitleScene())
                    return true
                }

                // 소리 ON/OFF: X: 200f ~ 700f, Y: 850f ~ 950f
                if (tx in 200f..700f && ty in 850f..950f) {
                    BGM.isSoundOn = !BGM.isSoundOn
                    SoundEffects.play(R.raw.heehee)
                    return true
                }
            }
            return true // 일시정지 상태에서는 일반 게임 스롭 터치를 차단하고 이벤트 소모
        }

        // 2. 일반 상태일 때 일시정지 버튼 터치 처리
        // PAUSE 버튼 영역: X: 200f ~ 400f, Y: 140f ~ 220f
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tx = event.x
            val ty = event.y
            if (tx in 650f..830f && ty in 160f..230f) {
                isPaused = true
                BGM.pause()
                SoundEffects.play(R.raw.heehee)
                return true
            }
        }

        // 3. 일반 게임 드롭 조종 터치 처리
        val photo = currentPhoto ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // 터치 조준 X 위치 보정 (안전 마진 10f 추가)
                val minX = 50f + photo.radius + 10f
                val maxX = 850f - photo.radius - 10f
                photo.x = event.x.coerceIn(minX, maxX)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (shootCooldown <= 0f) {
                    photo.isDropped = true
                    // 투하 시 Current 레이어에서 Stall 레이어로 마이그레이션
                    world.remove(Layer.Current, photo)
                    world.add(Layer.Stall, photo)
                    currentPhoto = null
                    shootCooldown = 0.7f // 투하 후 0.7초 스폰 대기
                }
                return true
            }
        }
        return false
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        
        if (!isGameOver) {
            drawPauseButton(canvas)
        }
        
        if (isPaused) {
            drawPauseMenu(canvas)
        }
    }

    private fun drawPauseButton(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.parseColor("#44FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // 버튼 테두리 그리기: X: 200f ~ 400f, Y: 140f ~ 220f
        val rect = RectF(650f, 160f, 830f, 230f)
        canvas.drawRoundRect(rect, 15f, 15f, paint)
        canvas.drawText("일시정지", 740f, 207f, textPaint)
    }

    private fun drawPauseMenu(canvas: Canvas) {
        // 1. 반투명 배경 장막
        val dimPaint = Paint().apply {
            color = Color.parseColor("#CC000000")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 900f, 1600f, dimPaint)

        // 2. 메뉴 박스 바디
        val boxPaint = Paint().apply {
            color = Color.parseColor("#1A1A2E")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        val menuRect = RectF(150f, 400f, 750f, 1050f)
        canvas.drawRoundRect(menuRect, 30f, 30f, boxPaint)
        canvas.drawRoundRect(menuRect, 30f, 30f, borderPaint)

        // 3. 타이틀 텍스트
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("일시정지", 450f, 480f, titlePaint)

        // 4. 세 개의 버튼 그리기
        val btnPaint = Paint().apply {
            color = Color.parseColor("#16213E")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val btnBorderPaint = Paint().apply {
            color = Color.parseColor("#44FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val btnTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // 버튼 1: RESUME (X: 200f ~ 700f, Y: 550f ~ 650f)
        val r1 = RectF(200f, 550f, 700f, 650f)
        canvas.drawRoundRect(r1, 15f, 15f, btnPaint)
        canvas.drawRoundRect(r1, 15f, 15f, btnBorderPaint)
        canvas.drawText("계속하기", 450f, 612f, btnTextPaint)

        // 버튼 2: GO TO LOBBY (X: 200f ~ 700f, Y: 700f ~ 800f)
        val r2 = RectF(200f, 700f, 700f, 800f)
        canvas.drawRoundRect(r2, 15f, 15f, btnPaint)
        canvas.drawRoundRect(r2, 15f, 15f, btnBorderPaint)
        canvas.drawText("로비로 가기", 450f, 762f, btnTextPaint)

        // 버튼 3: SOUND ON/OFF (X: 200f ~ 700f, Y: 850f ~ 950f)
        val r3 = RectF(200f, 850f, 700f, 950f)
        canvas.drawRoundRect(r3, 15f, 15f, btnPaint)
        canvas.drawRoundRect(r3, 15f, 15f, btnBorderPaint)
        val soundText = if (BGM.isSoundOn) "소리: 켬" else "소리: 끔"
        btnTextPaint.color = if (BGM.isSoundOn) Color.parseColor("#00E5FF") else Color.parseColor("#FF3366")
        canvas.drawText(soundText, 450f, 912f, btnTextPaint)
    }
}

