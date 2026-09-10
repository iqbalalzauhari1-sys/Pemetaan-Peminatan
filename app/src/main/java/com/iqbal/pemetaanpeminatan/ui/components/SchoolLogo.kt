package com.iqbal.pemetaanpeminatan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iqbal.pemetaanpeminatan.R

@Composable
fun SchoolLogo(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logosman1wanayasa),
            contentDescription = stringResource(R.string.content_description_school_logo),
            modifier = Modifier.size(250.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SchoolLogoPreview() {
    MaterialTheme {
        SchoolLogo()
    }
}
