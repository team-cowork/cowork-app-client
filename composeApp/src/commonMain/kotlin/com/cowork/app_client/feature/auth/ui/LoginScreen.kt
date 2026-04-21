package com.cowork.app_client.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import coworkappclient.composeapp.generated.resources.Res
import coworkappclient.composeapp.generated.resources.d_black
import com.cowork.app_client.feature.auth.component.AuthComponent
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(component: AuthComponent) {
    val state by component.state.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB91624)),
    ) {
        val compact = maxWidth < 920.dp
        val cardModifier = if (compact) {
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 424.dp)
        } else {
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 80.dp)
                .widthIn(min = 392.dp, max = 432.dp)
        }

        SplashBackground(modifier = Modifier.fillMaxSize())

        if (compact) {
            LoginCompactBrand(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            )
        } else {
            LoginSplashCopy(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 88.dp, end = 560.dp),
            )
        }

        Surface(
            modifier = cardModifier,
            color = Color(0xFF313338),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
        ) {
            LoginActionPane(
                isLoading = state.isLoading,
                error = state.error,
                onLoginClick = component::onLoginClick,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 36.dp),
            )
        }
    }
}

@Composable
private fun LoginCompactBrand(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "cowork",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "학교 프로젝트가 흩어지지 않도록.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.86f),
        )
    }
}

@Composable
private fun SplashBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFFF04452))

        val gridColor = Color.White.copy(alpha = 0.12f)
        val step = 36.dp.toPx()
        var x = -step
        while (x < size.width + step) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x + size.height * 0.45f, size.height),
                strokeWidth = 1.dp.toPx(),
            )
            x += step
        }

        val artCenterY = size.height * 0.5f
        val artX = 96.dp.toPx()
        val panelWidth = 430.dp.toPx()
        val panelHeight = 430.dp.toPx()
        val panelTop = artCenterY - panelHeight * 0.48f

        drawRoundRect(
            color = Color(0xFF8F1320).copy(alpha = 0.42f),
            topLeft = Offset(artX, panelTop),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.16f),
            topLeft = Offset(artX + 64.dp.toPx(), panelTop + 56.dp.toPx()),
            size = Size(270.dp.toPx(), 76.dp.toPx()),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.18f),
            topLeft = Offset(artX + 118.dp.toPx(), panelTop + 200.dp.toPx()),
            size = Size(420.dp.toPx(), 86.dp.toPx()),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        )
        drawRoundRect(
            color = Color(0xFF202225).copy(alpha = 0.45f),
            topLeft = Offset(artX + 280.dp.toPx(), panelTop + 318.dp.toPx()),
            size = Size(280.dp.toPx(), 120.dp.toPx()),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(size.width * 0.58f, -size.height * 0.08f),
            size = Size(size.width * 0.34f, size.height * 1.18f),
            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
        )
    }
}

@Composable
private fun LoginSplashCopy(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "cowork",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "학교 프로젝트가\n흩어지지 않도록.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "광주소프트웨어마이스터고등학교 팀을 위한 채팅, 이슈, 채널 기반 협업 공간입니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.82f),
            modifier = Modifier.widthIn(max = 460.dp),
        )
    }
}

@Composable
private fun LoginActionPane(
    isLoading: Boolean,
    error: String?,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "돌아오신 걸 환영해요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "DataGSM 계정으로 계속합니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB5BAC1),
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "브라우저에서 로그인을 완료해주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB5BAC1),
            )
        } else {
            DataGsmLoginButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 260.dp),
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFB4AB),
            )
        }
    }
}

@Composable
private fun DataGsmLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val shape = MaterialTheme.shapes.small

    Row(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (isHovered) Color(0xFFEFEFEF) else Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE5E5E5),
                shape = shape,
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.d_black),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "DataGSM으로 로그인",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.width(14.dp))
    }
}
