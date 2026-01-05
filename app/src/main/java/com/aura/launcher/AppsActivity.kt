try {
    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } else {
        Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
    }
} catch (e: Exception) {
    Toast.makeText(this, "Viga: ${e.message}", Toast.LENGTH_LONG).show()
}
