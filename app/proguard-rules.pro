# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android-optimize.txt

# Keep accessibility service
-keep class com.taehyeong.commutealarm.service.CommuteAccessibilityService { *; }

# Keep alarm receiver
-keep class com.taehyeong.commutealarm.service.AlarmReceiver { *; }
-keep class com.taehyeong.commutealarm.service.BootReceiver { *; }
