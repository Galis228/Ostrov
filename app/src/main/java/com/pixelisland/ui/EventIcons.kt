@file:Suppress("DEPRECATION")
package com.pixelisland.ui

import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoNotDisturbOff
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Earbuds
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnKeyOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.WifiTetheringOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.AirplanemodeInactive
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Battery2Bar
import androidx.compose.material.icons.outlined.Battery5Bar
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothAudio
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.DoNotDisturbOff
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Earbuds
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.EmojiObjects
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.FlightLand
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.HeadsetOff
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Highlight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.VpnKeyOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material.icons.outlined.WifiTetheringOff
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.AirplanemodeInactive
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.Battery2Bar
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brightness5
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DoNotDisturbOff
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Earbuds
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiObjects
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.HeadsetOff
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneCallback
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.VpnKeyOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material.icons.rounded.WifiTetheringOff
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.sharp.AcUnit
import androidx.compose.material.icons.sharp.Air
import androidx.compose.material.icons.sharp.AirplanemodeActive
import androidx.compose.material.icons.sharp.AirplanemodeInactive
import androidx.compose.material.icons.sharp.Alarm
import androidx.compose.material.icons.sharp.AlarmOn
import androidx.compose.material.icons.sharp.Album
import androidx.compose.material.icons.sharp.AutoAwesome
import androidx.compose.material.icons.sharp.AvTimer
import androidx.compose.material.icons.sharp.Battery2Bar
import androidx.compose.material.icons.sharp.Battery5Bar
import androidx.compose.material.icons.sharp.BatteryAlert
import androidx.compose.material.icons.sharp.BatteryChargingFull
import androidx.compose.material.icons.sharp.BatteryFull
import androidx.compose.material.icons.sharp.BatterySaver
import androidx.compose.material.icons.sharp.BatteryStd
import androidx.compose.material.icons.sharp.Bedtime
import androidx.compose.material.icons.sharp.Bluetooth
import androidx.compose.material.icons.sharp.BluetoothAudio
import androidx.compose.material.icons.sharp.BluetoothConnected
import androidx.compose.material.icons.sharp.BluetoothDisabled
import androidx.compose.material.icons.sharp.Bolt
import androidx.compose.material.icons.sharp.Brightness5
import androidx.compose.material.icons.sharp.Cake
import androidx.compose.material.icons.sharp.Call
import androidx.compose.material.icons.sharp.Celebration
import androidx.compose.material.icons.sharp.Chat
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Cloud
import androidx.compose.material.icons.sharp.CloudDone
import androidx.compose.material.icons.sharp.CloudDownload
import androidx.compose.material.icons.sharp.CloudQueue
import androidx.compose.material.icons.sharp.CloudUpload
import androidx.compose.material.icons.sharp.Coffee
import androidx.compose.material.icons.sharp.CreditCard
import androidx.compose.material.icons.sharp.DarkMode
import androidx.compose.material.icons.sharp.DeliveryDining
import androidx.compose.material.icons.sharp.Directions
import androidx.compose.material.icons.sharp.DirectionsBike
import androidx.compose.material.icons.sharp.DirectionsCar
import androidx.compose.material.icons.sharp.DirectionsRun
import androidx.compose.material.icons.sharp.DirectionsWalk
import androidx.compose.material.icons.sharp.DoNotDisturbOff
import androidx.compose.material.icons.sharp.DoNotDisturbOn
import androidx.compose.material.icons.sharp.Done
import androidx.compose.material.icons.sharp.Download
import androidx.compose.material.icons.sharp.Downloading
import androidx.compose.material.icons.sharp.Earbuds
import androidx.compose.material.icons.sharp.Eco
import androidx.compose.material.icons.sharp.ElectricBolt
import androidx.compose.material.icons.sharp.EmojiEmotions
import androidx.compose.material.icons.sharp.EmojiObjects
import androidx.compose.material.icons.sharp.EnergySavingsLeaf
import androidx.compose.material.icons.sharp.Equalizer
import androidx.compose.material.icons.sharp.Error
import androidx.compose.material.icons.sharp.Event
import androidx.compose.material.icons.sharp.Face
import androidx.compose.material.icons.sharp.Fastfood
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FiberManualRecord
import androidx.compose.material.icons.sharp.Fingerprint
import androidx.compose.material.icons.sharp.FitnessCenter
import androidx.compose.material.icons.sharp.Flag
import androidx.compose.material.icons.sharp.FlashlightOff
import androidx.compose.material.icons.sharp.FlashlightOn
import androidx.compose.material.icons.sharp.Flight
import androidx.compose.material.icons.sharp.FlightLand
import androidx.compose.material.icons.sharp.FlightTakeoff
import androidx.compose.material.icons.sharp.Forest
import androidx.compose.material.icons.sharp.Forum
import androidx.compose.material.icons.sharp.Games
import androidx.compose.material.icons.sharp.Grain
import androidx.compose.material.icons.sharp.GraphicEq
import androidx.compose.material.icons.sharp.Grass
import androidx.compose.material.icons.sharp.Headphones
import androidx.compose.material.icons.sharp.Headset
import androidx.compose.material.icons.sharp.HeadsetMic
import androidx.compose.material.icons.sharp.HeadsetOff
import androidx.compose.material.icons.sharp.Help
import androidx.compose.material.icons.sharp.Highlight
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material.icons.sharp.HourglassBottom
import androidx.compose.material.icons.sharp.HourglassTop
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Key
import androidx.compose.material.icons.sharp.Language
import androidx.compose.material.icons.sharp.LibraryMusic
import androidx.compose.material.icons.sharp.LightMode
import androidx.compose.material.icons.sharp.Lightbulb
import androidx.compose.material.icons.sharp.LocalCafe
import androidx.compose.material.icons.sharp.LocalFireDepartment
import androidx.compose.material.icons.sharp.LocalShipping
import androidx.compose.material.icons.sharp.LocalTaxi
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material.icons.sharp.LockOpen
import androidx.compose.material.icons.sharp.Mail
import androidx.compose.material.icons.sharp.Map
import androidx.compose.material.icons.sharp.Message
import androidx.compose.material.icons.sharp.Mic
import androidx.compose.material.icons.sharp.Mood
import androidx.compose.material.icons.sharp.MusicNote
import androidx.compose.material.icons.sharp.MusicOff
import androidx.compose.material.icons.sharp.Navigation
import androidx.compose.material.icons.sharp.NearMe
import androidx.compose.material.icons.sharp.NetworkCheck
import androidx.compose.material.icons.sharp.Nightlight
import androidx.compose.material.icons.sharp.NightsStay
import androidx.compose.material.icons.sharp.Notifications
import androidx.compose.material.icons.sharp.NotificationsActive
import androidx.compose.material.icons.sharp.NotificationsOff
import androidx.compose.material.icons.sharp.Park
import androidx.compose.material.icons.sharp.Payments
import androidx.compose.material.icons.sharp.Pets
import androidx.compose.material.icons.sharp.PhoneCallback
import androidx.compose.material.icons.sharp.PhoneInTalk
import androidx.compose.material.icons.sharp.PhotoCamera
import androidx.compose.material.icons.sharp.Place
import androidx.compose.material.icons.sharp.Podcasts
import androidx.compose.material.icons.sharp.Power
import androidx.compose.material.icons.sharp.PowerSettingsNew
import androidx.compose.material.icons.sharp.Psychology
import androidx.compose.material.icons.sharp.Public
import androidx.compose.material.icons.sharp.Radio
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material.icons.sharp.Reply
import androidx.compose.material.icons.sharp.Restaurant
import androidx.compose.material.icons.sharp.Rocket
import androidx.compose.material.icons.sharp.RocketLaunch
import androidx.compose.material.icons.sharp.Router
import androidx.compose.material.icons.sharp.Savings
import androidx.compose.material.icons.sharp.Schedule
import androidx.compose.material.icons.sharp.School
import androidx.compose.material.icons.sharp.Screenshot
import androidx.compose.material.icons.sharp.Security
import androidx.compose.material.icons.sharp.SelfImprovement
import androidx.compose.material.icons.sharp.Send
import androidx.compose.material.icons.sharp.SentimentSatisfied
import androidx.compose.material.icons.sharp.Shield
import androidx.compose.material.icons.sharp.ShoppingBag
import androidx.compose.material.icons.sharp.ShoppingCart
import androidx.compose.material.icons.sharp.SignalCellularAlt
import androidx.compose.material.icons.sharp.Snooze
import androidx.compose.material.icons.sharp.Spa
import androidx.compose.material.icons.sharp.Speaker
import androidx.compose.material.icons.sharp.SportsEsports
import androidx.compose.material.icons.sharp.SportsScore
import androidx.compose.material.icons.sharp.SportsSoccer
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material.icons.sharp.Sync
import androidx.compose.material.icons.sharp.Thermostat
import androidx.compose.material.icons.sharp.Thunderstorm
import androidx.compose.material.icons.sharp.Timer
import androidx.compose.material.icons.sharp.Toys
import androidx.compose.material.icons.sharp.Umbrella
import androidx.compose.material.icons.sharp.Upload
import androidx.compose.material.icons.sharp.VerifiedUser
import androidx.compose.material.icons.sharp.Vibration
import androidx.compose.material.icons.sharp.Videocam
import androidx.compose.material.icons.sharp.VolumeMute
import androidx.compose.material.icons.sharp.VolumeOff
import androidx.compose.material.icons.sharp.VolumeUp
import androidx.compose.material.icons.sharp.VpnKey
import androidx.compose.material.icons.sharp.VpnKeyOff
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material.icons.sharp.Watch
import androidx.compose.material.icons.sharp.Water
import androidx.compose.material.icons.sharp.WaterDrop
import androidx.compose.material.icons.sharp.WbCloudy
import androidx.compose.material.icons.sharp.WbSunny
import androidx.compose.material.icons.sharp.Whatshot
import androidx.compose.material.icons.sharp.Wifi
import androidx.compose.material.icons.sharp.WifiOff
import androidx.compose.material.icons.sharp.WifiTethering
import androidx.compose.material.icons.sharp.WifiTetheringOff
import androidx.compose.material.icons.sharp.Work
import androidx.compose.material.icons.twotone.AcUnit
import androidx.compose.material.icons.twotone.Air
import androidx.compose.material.icons.twotone.AirplanemodeActive
import androidx.compose.material.icons.twotone.AirplanemodeInactive
import androidx.compose.material.icons.twotone.Alarm
import androidx.compose.material.icons.twotone.AlarmOn
import androidx.compose.material.icons.twotone.Album
import androidx.compose.material.icons.twotone.AutoAwesome
import androidx.compose.material.icons.twotone.AvTimer
import androidx.compose.material.icons.twotone.Battery2Bar
import androidx.compose.material.icons.twotone.Battery5Bar
import androidx.compose.material.icons.twotone.BatteryAlert
import androidx.compose.material.icons.twotone.BatteryChargingFull
import androidx.compose.material.icons.twotone.BatteryFull
import androidx.compose.material.icons.twotone.BatterySaver
import androidx.compose.material.icons.twotone.BatteryStd
import androidx.compose.material.icons.twotone.Bedtime
import androidx.compose.material.icons.twotone.Bluetooth
import androidx.compose.material.icons.twotone.BluetoothAudio
import androidx.compose.material.icons.twotone.BluetoothConnected
import androidx.compose.material.icons.twotone.BluetoothDisabled
import androidx.compose.material.icons.twotone.Bolt
import androidx.compose.material.icons.twotone.Brightness5
import androidx.compose.material.icons.twotone.Cake
import androidx.compose.material.icons.twotone.Call
import androidx.compose.material.icons.twotone.Celebration
import androidx.compose.material.icons.twotone.Chat
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Cloud
import androidx.compose.material.icons.twotone.CloudDone
import androidx.compose.material.icons.twotone.CloudDownload
import androidx.compose.material.icons.twotone.CloudQueue
import androidx.compose.material.icons.twotone.CloudUpload
import androidx.compose.material.icons.twotone.Coffee
import androidx.compose.material.icons.twotone.CreditCard
import androidx.compose.material.icons.twotone.DarkMode
import androidx.compose.material.icons.twotone.DeliveryDining
import androidx.compose.material.icons.twotone.Directions
import androidx.compose.material.icons.twotone.DirectionsBike
import androidx.compose.material.icons.twotone.DirectionsCar
import androidx.compose.material.icons.twotone.DirectionsRun
import androidx.compose.material.icons.twotone.DirectionsWalk
import androidx.compose.material.icons.twotone.DoNotDisturbOff
import androidx.compose.material.icons.twotone.DoNotDisturbOn
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.Downloading
import androidx.compose.material.icons.twotone.Earbuds
import androidx.compose.material.icons.twotone.Eco
import androidx.compose.material.icons.twotone.ElectricBolt
import androidx.compose.material.icons.twotone.EmojiEmotions
import androidx.compose.material.icons.twotone.EmojiObjects
import androidx.compose.material.icons.twotone.EnergySavingsLeaf
import androidx.compose.material.icons.twotone.Equalizer
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material.icons.twotone.Event
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.Fastfood
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.FiberManualRecord
import androidx.compose.material.icons.twotone.Fingerprint
import androidx.compose.material.icons.twotone.FitnessCenter
import androidx.compose.material.icons.twotone.Flag
import androidx.compose.material.icons.twotone.FlashlightOff
import androidx.compose.material.icons.twotone.FlashlightOn
import androidx.compose.material.icons.twotone.Flight
import androidx.compose.material.icons.twotone.FlightLand
import androidx.compose.material.icons.twotone.FlightTakeoff
import androidx.compose.material.icons.twotone.Forest
import androidx.compose.material.icons.twotone.Forum
import androidx.compose.material.icons.twotone.Games
import androidx.compose.material.icons.twotone.Grain
import androidx.compose.material.icons.twotone.GraphicEq
import androidx.compose.material.icons.twotone.Grass
import androidx.compose.material.icons.twotone.Headphones
import androidx.compose.material.icons.twotone.Headset
import androidx.compose.material.icons.twotone.HeadsetMic
import androidx.compose.material.icons.twotone.HeadsetOff
import androidx.compose.material.icons.twotone.Help
import androidx.compose.material.icons.twotone.Highlight
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.HourglassBottom
import androidx.compose.material.icons.twotone.HourglassTop
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Key
import androidx.compose.material.icons.twotone.Language
import androidx.compose.material.icons.twotone.LibraryMusic
import androidx.compose.material.icons.twotone.LightMode
import androidx.compose.material.icons.twotone.Lightbulb
import androidx.compose.material.icons.twotone.LocalCafe
import androidx.compose.material.icons.twotone.LocalFireDepartment
import androidx.compose.material.icons.twotone.LocalShipping
import androidx.compose.material.icons.twotone.LocalTaxi
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.LockOpen
import androidx.compose.material.icons.twotone.Mail
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material.icons.twotone.Message
import androidx.compose.material.icons.twotone.Mic
import androidx.compose.material.icons.twotone.Mood
import androidx.compose.material.icons.twotone.MusicNote
import androidx.compose.material.icons.twotone.MusicOff
import androidx.compose.material.icons.twotone.Navigation
import androidx.compose.material.icons.twotone.NearMe
import androidx.compose.material.icons.twotone.NetworkCheck
import androidx.compose.material.icons.twotone.Nightlight
import androidx.compose.material.icons.twotone.NightsStay
import androidx.compose.material.icons.twotone.Notifications
import androidx.compose.material.icons.twotone.NotificationsActive
import androidx.compose.material.icons.twotone.NotificationsOff
import androidx.compose.material.icons.twotone.Park
import androidx.compose.material.icons.twotone.Payments
import androidx.compose.material.icons.twotone.Pets
import androidx.compose.material.icons.twotone.PhoneCallback
import androidx.compose.material.icons.twotone.PhoneInTalk
import androidx.compose.material.icons.twotone.PhotoCamera
import androidx.compose.material.icons.twotone.Place
import androidx.compose.material.icons.twotone.Podcasts
import androidx.compose.material.icons.twotone.Power
import androidx.compose.material.icons.twotone.PowerSettingsNew
import androidx.compose.material.icons.twotone.Psychology
import androidx.compose.material.icons.twotone.Public
import androidx.compose.material.icons.twotone.Radio
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.Reply
import androidx.compose.material.icons.twotone.Restaurant
import androidx.compose.material.icons.twotone.Rocket
import androidx.compose.material.icons.twotone.RocketLaunch
import androidx.compose.material.icons.twotone.Router
import androidx.compose.material.icons.twotone.Savings
import androidx.compose.material.icons.twotone.Schedule
import androidx.compose.material.icons.twotone.School
import androidx.compose.material.icons.twotone.Screenshot
import androidx.compose.material.icons.twotone.Security
import androidx.compose.material.icons.twotone.SelfImprovement
import androidx.compose.material.icons.twotone.Send
import androidx.compose.material.icons.twotone.SentimentSatisfied
import androidx.compose.material.icons.twotone.Shield
import androidx.compose.material.icons.twotone.ShoppingBag
import androidx.compose.material.icons.twotone.ShoppingCart
import androidx.compose.material.icons.twotone.SignalCellularAlt
import androidx.compose.material.icons.twotone.Snooze
import androidx.compose.material.icons.twotone.Spa
import androidx.compose.material.icons.twotone.Speaker
import androidx.compose.material.icons.twotone.SportsEsports
import androidx.compose.material.icons.twotone.SportsScore
import androidx.compose.material.icons.twotone.SportsSoccer
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material.icons.twotone.Sync
import androidx.compose.material.icons.twotone.Thermostat
import androidx.compose.material.icons.twotone.Thunderstorm
import androidx.compose.material.icons.twotone.Timer
import androidx.compose.material.icons.twotone.Toys
import androidx.compose.material.icons.twotone.Umbrella
import androidx.compose.material.icons.twotone.Upload
import androidx.compose.material.icons.twotone.VerifiedUser
import androidx.compose.material.icons.twotone.Vibration
import androidx.compose.material.icons.twotone.Videocam
import androidx.compose.material.icons.twotone.VolumeMute
import androidx.compose.material.icons.twotone.VolumeOff
import androidx.compose.material.icons.twotone.VolumeUp
import androidx.compose.material.icons.twotone.VpnKey
import androidx.compose.material.icons.twotone.VpnKeyOff
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material.icons.twotone.Watch
import androidx.compose.material.icons.twotone.Water
import androidx.compose.material.icons.twotone.WaterDrop
import androidx.compose.material.icons.twotone.WbCloudy
import androidx.compose.material.icons.twotone.WbSunny
import androidx.compose.material.icons.twotone.Whatshot
import androidx.compose.material.icons.twotone.Wifi
import androidx.compose.material.icons.twotone.WifiOff
import androidx.compose.material.icons.twotone.WifiTethering
import androidx.compose.material.icons.twotone.WifiTetheringOff
import androidx.compose.material.icons.twotone.Work
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.sharp.BrightnessMedium
import androidx.compose.material.icons.twotone.BrightnessMedium
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.sharp.ScreenRotation
import androidx.compose.material.icons.twotone.ScreenRotation
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.outlined.ScreenLockRotation
import androidx.compose.material.icons.sharp.ScreenLockRotation
import androidx.compose.material.icons.twotone.ScreenLockRotation
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material.icons.sharp.SignalWifiOff
import androidx.compose.material.icons.twotone.SignalWifiOff
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.outlined.SignalWifi4Bar
import androidx.compose.material.icons.sharp.SignalWifi4Bar
import androidx.compose.material.icons.twotone.SignalWifi4Bar
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.sharp.Inventory2
import androidx.compose.material.icons.twotone.Inventory2
import androidx.compose.material.icons.rounded.SignalCellularOff
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.outlined.SignalCellularOff
import androidx.compose.material.icons.sharp.SignalCellularOff
import androidx.compose.material.icons.twotone.SignalCellularOff
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.twotone.Speed
import androidx.compose.material.icons.rounded.TurnLeft
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.outlined.TurnLeft
import androidx.compose.material.icons.sharp.TurnLeft
import androidx.compose.material.icons.twotone.TurnLeft
import androidx.compose.material.icons.rounded.TurnRight
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.outlined.TurnRight
import androidx.compose.material.icons.sharp.TurnRight
import androidx.compose.material.icons.twotone.TurnRight
import androidx.compose.material.icons.rounded.Straight
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.outlined.Straight
import androidx.compose.material.icons.sharp.Straight
import androidx.compose.material.icons.twotone.Straight
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.sharp.CurrencyExchange
import androidx.compose.material.icons.twotone.CurrencyExchange
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.sharp.ShowChart
import androidx.compose.material.icons.twotone.ShowChart
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.sharp.Layers
import androidx.compose.material.icons.twotone.Layers
import androidx.compose.material.icons.rounded.PhoneMissed
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.outlined.PhoneMissed
import androidx.compose.material.icons.sharp.PhoneMissed
import androidx.compose.material.icons.twotone.PhoneMissed
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.sharp.KeyboardVoice
import androidx.compose.material.icons.twotone.KeyboardVoice
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.sharp.CurrencyBitcoin
import androidx.compose.material.icons.twotone.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.sharp.Wallpaper
import androidx.compose.material.icons.twotone.Wallpaper
import androidx.compose.ui.graphics.vector.ImageVector
import com.pixelisland.settings.EventType
import com.pixelisland.settings.IconVariant

/** Иконка события: своя из каталога или стандартная, в выбранном стиле Material. */
fun iconFor(t: EventType, v: IconVariant, custom: String = ""): ImageVector =
    if (custom.isNotEmpty()) catalogIcon(custom, v) ?: defaultIcon(t, v) else defaultIcon(t, v)

private fun defaultIcon(t: EventType, v: IconVariant): ImageVector = when (t) {
    EventType.MUSIC -> catalogIcon("MusicNote", v)!!
    EventType.CALL -> catalogIcon("Call", v)!!
    EventType.TIMER -> catalogIcon("Timer", v)!!
    EventType.LIVE_UPDATE -> catalogIcon("Navigation", v)!!
    EventType.PROGRESS -> catalogIcon("Download", v)!!
    EventType.CHARGE_START -> catalogIcon("BatteryChargingFull", v)!!
    EventType.CHARGE_STOP -> catalogIcon("BatteryStd", v)!!
    EventType.CHARGE_FULL -> catalogIcon("BatteryFull", v)!!
    EventType.BATTERY_LOW -> catalogIcon("BatteryAlert", v)!!
    EventType.CHARGING_LIVE -> catalogIcon("Bolt", v)!!
    EventType.SAVER_ON -> catalogIcon("EnergySavingsLeaf", v)!!
    EventType.SAVER_OFF -> catalogIcon("BatterySaver", v)!!
    EventType.BT_ON -> catalogIcon("Bluetooth", v)!!
    EventType.BT_OFF -> catalogIcon("BluetoothDisabled", v)!!
    EventType.HP_ON -> catalogIcon("Headphones", v)!!
    EventType.HP_OFF -> catalogIcon("HeadsetOff", v)!!
    EventType.WIFI_ON -> catalogIcon("Wifi", v)!!
    EventType.WIFI_OFF -> catalogIcon("WifiOff", v)!!
    EventType.AIR_ON -> catalogIcon("AirplanemodeActive", v)!!
    EventType.AIR_OFF -> catalogIcon("AirplanemodeInactive", v)!!
    EventType.VPN_ON -> catalogIcon("VpnKey", v)!!
    EventType.VPN_OFF -> catalogIcon("VpnKeyOff", v)!!
    EventType.HOTSPOT_ON -> catalogIcon("WifiTethering", v)!!
    EventType.HOTSPOT_OFF -> catalogIcon("WifiTetheringOff", v)!!
    EventType.RINGER_SOUND -> catalogIcon("NotificationsActive", v)!!
    EventType.RINGER_VIBRATE -> catalogIcon("Vibration", v)!!
    EventType.RINGER_SILENT -> catalogIcon("NotificationsOff", v)!!
    EventType.DND_ON -> catalogIcon("DoNotDisturbOn", v)!!
    EventType.DND_OFF -> catalogIcon("DoNotDisturbOff", v)!!
    EventType.VOLUME -> catalogIcon("VolumeUp", v)!!
    EventType.RECORDING -> catalogIcon("FiberManualRecord", v)!!
    EventType.MIC_IN_USE -> catalogIcon("Mic", v)!!
    EventType.CAMERA_IN_USE -> catalogIcon("PhotoCamera", v)!!
    EventType.ALARM_SOON -> catalogIcon("Alarm", v)!!
    EventType.ALARM_RING -> catalogIcon("AlarmOn", v)!!
    EventType.CALENDAR -> catalogIcon("Event", v)!!
    EventType.WEATHER -> catalogIcon("WbSunny", v)!!
    EventType.SCREENSHOT -> catalogIcon("Screenshot", v)!!
    EventType.OWN_TIMER -> catalogIcon("AvTimer", v)!!
    EventType.DELIVERY -> catalogIcon("LocalShipping", v)!!
    EventType.SCORE -> catalogIcon("SportsSoccer", v)!!
    EventType.PAYMENT -> catalogIcon("Payments", v)!!
    EventType.TORCH_ON -> catalogIcon("FlashlightOn", v)!!
    EventType.TORCH_OFF -> catalogIcon("FlashlightOff", v)!!
    EventType.UNLOCK -> catalogIcon("LockOpen", v)!!
    EventType.NOTIF_PEEK -> catalogIcon("Notifications", v)!!
    EventType.NAVIGATION -> catalogIcon("TurnRight", v)!!
    EventType.VOICE -> catalogIcon("KeyboardVoice", v)!!
    EventType.NOW_PLAYING -> catalogIcon("GraphicEq", v)!!
    EventType.TRANSFER -> catalogIcon("Share", v)!!
    EventType.NET_LOST -> catalogIcon("SignalWifiOff", v)!!
    EventType.NET_BACK -> catalogIcon("SignalWifi4Bar", v)!!
    EventType.FOCUS -> catalogIcon("Nightlight", v)!!
    EventType.BRIGHTNESS -> catalogIcon("BrightnessMedium", v)!!
    EventType.ROTATE -> catalogIcon("ScreenRotation", v)!!
    EventType.COPIED -> catalogIcon("ContentCopy", v)!!
    EventType.WEATHER_ALERT -> catalogIcon("Umbrella", v)!!
    EventType.SHELF -> catalogIcon("Inventory2", v)!!
}

data class CatalogIcon(val name: String, val group: String)

val IconCatalog: List<CatalogIcon> = listOf(
    CatalogIcon("Bolt", "Энергия"),
    CatalogIcon("ElectricBolt", "Энергия"),
    CatalogIcon("Power", "Энергия"),
    CatalogIcon("PowerSettingsNew", "Энергия"),
    CatalogIcon("BatteryChargingFull", "Энергия"),
    CatalogIcon("BatteryFull", "Энергия"),
    CatalogIcon("Battery5Bar", "Энергия"),
    CatalogIcon("Battery2Bar", "Энергия"),
    CatalogIcon("EnergySavingsLeaf", "Энергия"),
    CatalogIcon("Eco", "Энергия"),
    CatalogIcon("Bluetooth", "Связь"),
    CatalogIcon("BluetoothConnected", "Связь"),
    CatalogIcon("BluetoothAudio", "Связь"),
    CatalogIcon("Wifi", "Связь"),
    CatalogIcon("WifiOff", "Связь"),
    CatalogIcon("SignalCellularAlt", "Связь"),
    CatalogIcon("NetworkCheck", "Связь"),
    CatalogIcon("Router", "Связь"),
    CatalogIcon("Cloud", "Связь"),
    CatalogIcon("CloudDone", "Связь"),
    CatalogIcon("Public", "Связь"),
    CatalogIcon("Language", "Связь"),
    CatalogIcon("VpnKey", "Связь"),
    CatalogIcon("WifiTethering", "Связь"),
    CatalogIcon("Headphones", "Звук"),
    CatalogIcon("Headset", "Звук"),
    CatalogIcon("HeadsetMic", "Звук"),
    CatalogIcon("Earbuds", "Звук"),
    CatalogIcon("Speaker", "Звук"),
    CatalogIcon("NotificationsActive", "Звук"),
    CatalogIcon("Notifications", "Звук"),
    CatalogIcon("NotificationsOff", "Звук"),
    CatalogIcon("Vibration", "Звук"),
    CatalogIcon("VolumeUp", "Звук"),
    CatalogIcon("VolumeOff", "Звук"),
    CatalogIcon("VolumeMute", "Звук"),
    CatalogIcon("MusicNote", "Звук"),
    CatalogIcon("MusicOff", "Звук"),
    CatalogIcon("LibraryMusic", "Звук"),
    CatalogIcon("Album", "Звук"),
    CatalogIcon("GraphicEq", "Звук"),
    CatalogIcon("Equalizer", "Звук"),
    CatalogIcon("Radio", "Звук"),
    CatalogIcon("Podcasts", "Звук"),
    CatalogIcon("Mic", "Звук"),
    CatalogIcon("WbSunny", "Погода"),
    CatalogIcon("WbCloudy", "Погода"),
    CatalogIcon("CloudQueue", "Погода"),
    CatalogIcon("Thunderstorm", "Погода"),
    CatalogIcon("Grain", "Погода"),
    CatalogIcon("Umbrella", "Погода"),
    CatalogIcon("AcUnit", "Погода"),
    CatalogIcon("Air", "Погода"),
    CatalogIcon("Thermostat", "Погода"),
    CatalogIcon("Water", "Погода"),
    CatalogIcon("WaterDrop", "Погода"),
    CatalogIcon("AirplanemodeActive", "Полёт и путь"),
    CatalogIcon("Flight", "Полёт и путь"),
    CatalogIcon("FlightTakeoff", "Полёт и путь"),
    CatalogIcon("FlightLand", "Полёт и путь"),
    CatalogIcon("Rocket", "Полёт и путь"),
    CatalogIcon("RocketLaunch", "Полёт и путь"),
    CatalogIcon("Navigation", "Полёт и путь"),
    CatalogIcon("ContentCopy", "Система"),
    CatalogIcon("BrightnessMedium", "Система"),
    CatalogIcon("ScreenRotation", "Система"),
    CatalogIcon("ScreenLockRotation", "Система"),
    CatalogIcon("SignalWifiOff", "Связь"),
    CatalogIcon("SignalWifi4Bar", "Связь"),
    CatalogIcon("Share", "Связь"),
    CatalogIcon("Inventory2", "Жизнь"),
    CatalogIcon("SignalCellularOff", "Связь"),
    CatalogIcon("Speed", "Связь"),
    CatalogIcon("TurnLeft", "Полёт и путь"),
    CatalogIcon("TurnRight", "Полёт и путь"),
    CatalogIcon("Straight", "Полёт и путь"),
    CatalogIcon("Fingerprint", "Система"),
    CatalogIcon("CurrencyExchange", "Жизнь"),
    CatalogIcon("ShowChart", "Жизнь"),
    CatalogIcon("Layers", "Жизнь"),
    CatalogIcon("PhoneMissed", "Связь"),
    CatalogIcon("KeyboardVoice", "Звук"),
    CatalogIcon("Nightlight", "Время"),
    CatalogIcon("CurrencyBitcoin", "Жизнь"),
    CatalogIcon("Wallpaper", "Жизнь"),
    CatalogIcon("NearMe", "Полёт и путь"),
    CatalogIcon("Directions", "Полёт и путь"),
    CatalogIcon("DirectionsCar", "Полёт и путь"),
    CatalogIcon("DirectionsBike", "Полёт и путь"),
    CatalogIcon("DirectionsWalk", "Полёт и путь"),
    CatalogIcon("DirectionsRun", "Полёт и путь"),
    CatalogIcon("LocalShipping", "Полёт и путь"),
    CatalogIcon("DeliveryDining", "Полёт и путь"),
    CatalogIcon("LocalTaxi", "Полёт и путь"),
    CatalogIcon("Map", "Полёт и путь"),
    CatalogIcon("Place", "Полёт и путь"),
    CatalogIcon("DoNotDisturbOn", "День и ночь"),
    CatalogIcon("Bedtime", "День и ночь"),
    CatalogIcon("NightsStay", "День и ночь"),
    CatalogIcon("DarkMode", "День и ночь"),
    CatalogIcon("LightMode", "День и ночь"),
    CatalogIcon("Brightness5", "День и ночь"),
    CatalogIcon("Grass", "День и ночь"),
    CatalogIcon("Park", "День и ночь"),
    CatalogIcon("Forest", "День и ночь"),
    CatalogIcon("FlashlightOn", "Свет"),
    CatalogIcon("FlashlightOff", "Свет"),
    CatalogIcon("Highlight", "Свет"),
    CatalogIcon("Lightbulb", "Свет"),
    CatalogIcon("EmojiObjects", "Свет"),
    CatalogIcon("AutoAwesome", "Свет"),
    CatalogIcon("Whatshot", "Свет"),
    CatalogIcon("LocalFireDepartment", "Свет"),
    CatalogIcon("LockOpen", "Безопасность"),
    CatalogIcon("Lock", "Безопасность"),
    CatalogIcon("Face", "Безопасность"),
    CatalogIcon("Key", "Безопасность"),
    CatalogIcon("Security", "Безопасность"),
    CatalogIcon("Shield", "Безопасность"),
    CatalogIcon("VerifiedUser", "Безопасность"),
    CatalogIcon("PhotoCamera", "Безопасность"),
    CatalogIcon("Videocam", "Безопасность"),
    CatalogIcon("Screenshot", "Безопасность"),
    CatalogIcon("Call", "Общение"),
    CatalogIcon("PhoneInTalk", "Общение"),
    CatalogIcon("PhoneCallback", "Общение"),
    CatalogIcon("Chat", "Общение"),
    CatalogIcon("Message", "Общение"),
    CatalogIcon("Mail", "Общение"),
    CatalogIcon("Send", "Общение"),
    CatalogIcon("Forum", "Общение"),
    CatalogIcon("Reply", "Общение"),
    CatalogIcon("Timer", "Время"),
    CatalogIcon("HourglassBottom", "Время"),
    CatalogIcon("HourglassTop", "Время"),
    CatalogIcon("AvTimer", "Время"),
    CatalogIcon("Schedule", "Время"),
    CatalogIcon("Alarm", "Время"),
    CatalogIcon("AlarmOn", "Время"),
    CatalogIcon("Snooze", "Время"),
    CatalogIcon("Event", "Время"),
    CatalogIcon("Download", "Загрузки"),
    CatalogIcon("Upload", "Загрузки"),
    CatalogIcon("CloudDownload", "Загрузки"),
    CatalogIcon("CloudUpload", "Загрузки"),
    CatalogIcon("Sync", "Загрузки"),
    CatalogIcon("Refresh", "Загрузки"),
    CatalogIcon("Downloading", "Загрузки"),
    CatalogIcon("SportsSoccer", "Жизнь"),
    CatalogIcon("SportsScore", "Жизнь"),
    CatalogIcon("Payments", "Жизнь"),
    CatalogIcon("Flag", "Жизнь"),
    CatalogIcon("Favorite", "Жизнь"),
    CatalogIcon("Star", "Жизнь"),
    CatalogIcon("Pets", "Жизнь"),
    CatalogIcon("Coffee", "Жизнь"),
    CatalogIcon("LocalCafe", "Жизнь"),
    CatalogIcon("Restaurant", "Жизнь"),
    CatalogIcon("Fastfood", "Жизнь"),
    CatalogIcon("Cake", "Жизнь"),
    CatalogIcon("Celebration", "Жизнь"),
    CatalogIcon("SportsEsports", "Жизнь"),
    CatalogIcon("Games", "Жизнь"),
    CatalogIcon("Toys", "Жизнь"),
    CatalogIcon("Spa", "Жизнь"),
    CatalogIcon("SelfImprovement", "Жизнь"),
    CatalogIcon("FitnessCenter", "Жизнь"),
    CatalogIcon("Home", "Жизнь"),
    CatalogIcon("Work", "Жизнь"),
    CatalogIcon("School", "Жизнь"),
    CatalogIcon("ShoppingCart", "Жизнь"),
    CatalogIcon("ShoppingBag", "Жизнь"),
    CatalogIcon("Savings", "Жизнь"),
    CatalogIcon("CreditCard", "Жизнь"),
    CatalogIcon("Watch", "Жизнь"),
    CatalogIcon("Psychology", "Настроение"),
    CatalogIcon("Mood", "Настроение"),
    CatalogIcon("SentimentSatisfied", "Настроение"),
    CatalogIcon("EmojiEmotions", "Настроение"),
    CatalogIcon("Check", "Настроение"),
    CatalogIcon("CheckCircle", "Настроение"),
    CatalogIcon("Done", "Настроение"),
    CatalogIcon("Warning", "Настроение"),
    CatalogIcon("Error", "Настроение"),
    CatalogIcon("Info", "Настроение"),
    CatalogIcon("Help", "Настроение"),
    CatalogIcon("FiberManualRecord", "Настроение"),
)

fun catalogIcon(name: String, v: IconVariant): ImageVector? = when (v) {
    IconVariant.ROUNDED -> when (name) {
        "AcUnit" -> Icons.Rounded.AcUnit
        "Air" -> Icons.Rounded.Air
        "AirplanemodeActive" -> Icons.Rounded.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.Rounded.AirplanemodeInactive
        "Alarm" -> Icons.Rounded.Alarm
        "AlarmOn" -> Icons.Rounded.AlarmOn
        "Album" -> Icons.Rounded.Album
        "AutoAwesome" -> Icons.Rounded.AutoAwesome
        "AvTimer" -> Icons.Rounded.AvTimer
        "Battery2Bar" -> Icons.Rounded.Battery2Bar
        "Battery5Bar" -> Icons.Rounded.Battery5Bar
        "BatteryAlert" -> Icons.Rounded.BatteryAlert
        "BatteryChargingFull" -> Icons.Rounded.BatteryChargingFull
        "BatteryFull" -> Icons.Rounded.BatteryFull
        "BatterySaver" -> Icons.Rounded.BatterySaver
        "BatteryStd" -> Icons.Rounded.BatteryStd
        "Bedtime" -> Icons.Rounded.Bedtime
        "Bluetooth" -> Icons.Rounded.Bluetooth
        "BluetoothAudio" -> Icons.Rounded.BluetoothAudio
        "BluetoothConnected" -> Icons.Rounded.BluetoothConnected
        "BluetoothDisabled" -> Icons.Rounded.BluetoothDisabled
        "Bolt" -> Icons.Rounded.Bolt
        "Brightness5" -> Icons.Rounded.Brightness5
        "Cake" -> Icons.Rounded.Cake
        "Call" -> Icons.Rounded.Call
        "Celebration" -> Icons.Rounded.Celebration
        "Chat" -> Icons.Rounded.Chat
        "Check" -> Icons.Rounded.Check
        "CheckCircle" -> Icons.Rounded.CheckCircle
        "Cloud" -> Icons.Rounded.Cloud
        "CloudDone" -> Icons.Rounded.CloudDone
        "CloudDownload" -> Icons.Rounded.CloudDownload
        "CloudQueue" -> Icons.Rounded.CloudQueue
        "CloudUpload" -> Icons.Rounded.CloudUpload
        "Coffee" -> Icons.Rounded.Coffee
        "CreditCard" -> Icons.Rounded.CreditCard
        "DarkMode" -> Icons.Rounded.DarkMode
        "DeliveryDining" -> Icons.Rounded.DeliveryDining
        "Directions" -> Icons.Rounded.Directions
        "DirectionsBike" -> Icons.Rounded.DirectionsBike
        "DirectionsCar" -> Icons.Rounded.DirectionsCar
        "DirectionsRun" -> Icons.Rounded.DirectionsRun
        "DirectionsWalk" -> Icons.Rounded.DirectionsWalk
        "DoNotDisturbOff" -> Icons.Rounded.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.Rounded.DoNotDisturbOn
        "Done" -> Icons.Rounded.Done
        "Download" -> Icons.Rounded.Download
        "Downloading" -> Icons.Rounded.Downloading
        "Earbuds" -> Icons.Rounded.Earbuds
        "Eco" -> Icons.Rounded.Eco
        "ElectricBolt" -> Icons.Rounded.ElectricBolt
        "EmojiEmotions" -> Icons.Rounded.EmojiEmotions
        "EmojiObjects" -> Icons.Rounded.EmojiObjects
        "EnergySavingsLeaf" -> Icons.Rounded.EnergySavingsLeaf
        "Equalizer" -> Icons.Rounded.Equalizer
        "Error" -> Icons.Rounded.Error
        "Event" -> Icons.Rounded.Event
        "Face" -> Icons.Rounded.Face
        "Fastfood" -> Icons.Rounded.Fastfood
        "Favorite" -> Icons.Rounded.Favorite
        "FiberManualRecord" -> Icons.Rounded.FiberManualRecord
        "Fingerprint" -> Icons.Rounded.Fingerprint
        "FitnessCenter" -> Icons.Rounded.FitnessCenter
        "Flag" -> Icons.Rounded.Flag
        "FlashlightOff" -> Icons.Rounded.FlashlightOff
        "FlashlightOn" -> Icons.Rounded.FlashlightOn
        "Flight" -> Icons.Rounded.Flight
        "FlightLand" -> Icons.Rounded.FlightLand
        "FlightTakeoff" -> Icons.Rounded.FlightTakeoff
        "Forest" -> Icons.Rounded.Forest
        "Forum" -> Icons.Rounded.Forum
        "Games" -> Icons.Rounded.Games
        "Grain" -> Icons.Rounded.Grain
        "GraphicEq" -> Icons.Rounded.GraphicEq
        "Grass" -> Icons.Rounded.Grass
        "Headphones" -> Icons.Rounded.Headphones
        "Headset" -> Icons.Rounded.Headset
        "HeadsetMic" -> Icons.Rounded.HeadsetMic
        "HeadsetOff" -> Icons.Rounded.HeadsetOff
        "Help" -> Icons.Rounded.Help
        "Highlight" -> Icons.Rounded.Highlight
        "Home" -> Icons.Rounded.Home
        "HourglassBottom" -> Icons.Rounded.HourglassBottom
        "HourglassTop" -> Icons.Rounded.HourglassTop
        "Info" -> Icons.Rounded.Info
        "Key" -> Icons.Rounded.Key
        "Language" -> Icons.Rounded.Language
        "LibraryMusic" -> Icons.Rounded.LibraryMusic
        "LightMode" -> Icons.Rounded.LightMode
        "Lightbulb" -> Icons.Rounded.Lightbulb
        "LocalCafe" -> Icons.Rounded.LocalCafe
        "LocalFireDepartment" -> Icons.Rounded.LocalFireDepartment
        "LocalShipping" -> Icons.Rounded.LocalShipping
        "LocalTaxi" -> Icons.Rounded.LocalTaxi
        "Lock" -> Icons.Rounded.Lock
        "LockOpen" -> Icons.Rounded.LockOpen
        "Mail" -> Icons.Rounded.Mail
        "Map" -> Icons.Rounded.Map
        "Message" -> Icons.Rounded.Message
        "Mic" -> Icons.Rounded.Mic
        "Mood" -> Icons.Rounded.Mood
        "MusicNote" -> Icons.Rounded.MusicNote
        "MusicOff" -> Icons.Rounded.MusicOff
        "Navigation" -> Icons.Rounded.Navigation
        "ContentCopy" -> Icons.Rounded.ContentCopy
        "BrightnessMedium" -> Icons.Rounded.BrightnessMedium
        "ScreenRotation" -> Icons.Rounded.ScreenRotation
        "ScreenLockRotation" -> Icons.Rounded.ScreenLockRotation
        "SignalWifiOff" -> Icons.Rounded.SignalWifiOff
        "SignalWifi4Bar" -> Icons.Rounded.SignalWifi4Bar
        "Share" -> Icons.Rounded.Share
        "Inventory2" -> Icons.Rounded.Inventory2
        "SignalCellularOff" -> Icons.Rounded.SignalCellularOff
        "Speed" -> Icons.Rounded.Speed
        "TurnLeft" -> Icons.Rounded.TurnLeft
        "TurnRight" -> Icons.Rounded.TurnRight
        "Straight" -> Icons.Rounded.Straight
        "CurrencyExchange" -> Icons.Rounded.CurrencyExchange
        "ShowChart" -> Icons.Rounded.ShowChart
        "Layers" -> Icons.Rounded.Layers
        "PhoneMissed" -> Icons.Rounded.PhoneMissed
        "KeyboardVoice" -> Icons.Rounded.KeyboardVoice
        "Nightlight" -> Icons.Rounded.Nightlight
        "CurrencyBitcoin" -> Icons.Rounded.CurrencyBitcoin
        "Wallpaper" -> Icons.Rounded.Wallpaper
        "NearMe" -> Icons.Rounded.NearMe
        "NetworkCheck" -> Icons.Rounded.NetworkCheck
        "NightsStay" -> Icons.Rounded.NightsStay
        "Notifications" -> Icons.Rounded.Notifications
        "NotificationsActive" -> Icons.Rounded.NotificationsActive
        "NotificationsOff" -> Icons.Rounded.NotificationsOff
        "Park" -> Icons.Rounded.Park
        "Payments" -> Icons.Rounded.Payments
        "Pets" -> Icons.Rounded.Pets
        "PhoneCallback" -> Icons.Rounded.PhoneCallback
        "PhoneInTalk" -> Icons.Rounded.PhoneInTalk
        "PhotoCamera" -> Icons.Rounded.PhotoCamera
        "Place" -> Icons.Rounded.Place
        "Podcasts" -> Icons.Rounded.Podcasts
        "Power" -> Icons.Rounded.Power
        "PowerSettingsNew" -> Icons.Rounded.PowerSettingsNew
        "Psychology" -> Icons.Rounded.Psychology
        "Public" -> Icons.Rounded.Public
        "Radio" -> Icons.Rounded.Radio
        "Refresh" -> Icons.Rounded.Refresh
        "Reply" -> Icons.Rounded.Reply
        "Restaurant" -> Icons.Rounded.Restaurant
        "Rocket" -> Icons.Rounded.Rocket
        "RocketLaunch" -> Icons.Rounded.RocketLaunch
        "Router" -> Icons.Rounded.Router
        "Savings" -> Icons.Rounded.Savings
        "Schedule" -> Icons.Rounded.Schedule
        "School" -> Icons.Rounded.School
        "Screenshot" -> Icons.Rounded.Screenshot
        "Security" -> Icons.Rounded.Security
        "SelfImprovement" -> Icons.Rounded.SelfImprovement
        "Send" -> Icons.Rounded.Send
        "SentimentSatisfied" -> Icons.Rounded.SentimentSatisfied
        "Shield" -> Icons.Rounded.Shield
        "ShoppingBag" -> Icons.Rounded.ShoppingBag
        "ShoppingCart" -> Icons.Rounded.ShoppingCart
        "SignalCellularAlt" -> Icons.Rounded.SignalCellularAlt
        "Snooze" -> Icons.Rounded.Snooze
        "Spa" -> Icons.Rounded.Spa
        "Speaker" -> Icons.Rounded.Speaker
        "SportsEsports" -> Icons.Rounded.SportsEsports
        "SportsScore" -> Icons.Rounded.SportsScore
        "SportsSoccer" -> Icons.Rounded.SportsSoccer
        "Star" -> Icons.Rounded.Star
        "Sync" -> Icons.Rounded.Sync
        "Thermostat" -> Icons.Rounded.Thermostat
        "Thunderstorm" -> Icons.Rounded.Thunderstorm
        "Timer" -> Icons.Rounded.Timer
        "Toys" -> Icons.Rounded.Toys
        "Umbrella" -> Icons.Rounded.Umbrella
        "Upload" -> Icons.Rounded.Upload
        "VerifiedUser" -> Icons.Rounded.VerifiedUser
        "Vibration" -> Icons.Rounded.Vibration
        "Videocam" -> Icons.Rounded.Videocam
        "VolumeMute" -> Icons.Rounded.VolumeMute
        "VolumeOff" -> Icons.Rounded.VolumeOff
        "VolumeUp" -> Icons.Rounded.VolumeUp
        "VpnKey" -> Icons.Rounded.VpnKey
        "VpnKeyOff" -> Icons.Rounded.VpnKeyOff
        "Warning" -> Icons.Rounded.Warning
        "Watch" -> Icons.Rounded.Watch
        "Water" -> Icons.Rounded.Water
        "WaterDrop" -> Icons.Rounded.WaterDrop
        "WbCloudy" -> Icons.Rounded.WbCloudy
        "WbSunny" -> Icons.Rounded.WbSunny
        "Whatshot" -> Icons.Rounded.Whatshot
        "Wifi" -> Icons.Rounded.Wifi
        "WifiOff" -> Icons.Rounded.WifiOff
        "WifiTethering" -> Icons.Rounded.WifiTethering
        "WifiTetheringOff" -> Icons.Rounded.WifiTetheringOff
        "Work" -> Icons.Rounded.Work
        else -> null
    }
    IconVariant.FILLED -> when (name) {
        "AcUnit" -> Icons.Filled.AcUnit
        "Air" -> Icons.Filled.Air
        "AirplanemodeActive" -> Icons.Filled.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.Filled.AirplanemodeInactive
        "Alarm" -> Icons.Filled.Alarm
        "AlarmOn" -> Icons.Filled.AlarmOn
        "Album" -> Icons.Filled.Album
        "AutoAwesome" -> Icons.Filled.AutoAwesome
        "AvTimer" -> Icons.Filled.AvTimer
        "Battery2Bar" -> Icons.Filled.Battery2Bar
        "Battery5Bar" -> Icons.Filled.Battery5Bar
        "BatteryAlert" -> Icons.Filled.BatteryAlert
        "BatteryChargingFull" -> Icons.Filled.BatteryChargingFull
        "BatteryFull" -> Icons.Filled.BatteryFull
        "BatterySaver" -> Icons.Filled.BatterySaver
        "BatteryStd" -> Icons.Filled.BatteryStd
        "Bedtime" -> Icons.Filled.Bedtime
        "Bluetooth" -> Icons.Filled.Bluetooth
        "BluetoothAudio" -> Icons.Filled.BluetoothAudio
        "BluetoothConnected" -> Icons.Filled.BluetoothConnected
        "BluetoothDisabled" -> Icons.Filled.BluetoothDisabled
        "Bolt" -> Icons.Filled.Bolt
        "Brightness5" -> Icons.Filled.Brightness5
        "Cake" -> Icons.Filled.Cake
        "Call" -> Icons.Filled.Call
        "Celebration" -> Icons.Filled.Celebration
        "Chat" -> Icons.Filled.Chat
        "Check" -> Icons.Filled.Check
        "CheckCircle" -> Icons.Filled.CheckCircle
        "Cloud" -> Icons.Filled.Cloud
        "CloudDone" -> Icons.Filled.CloudDone
        "CloudDownload" -> Icons.Filled.CloudDownload
        "CloudQueue" -> Icons.Filled.CloudQueue
        "CloudUpload" -> Icons.Filled.CloudUpload
        "Coffee" -> Icons.Filled.Coffee
        "CreditCard" -> Icons.Filled.CreditCard
        "DarkMode" -> Icons.Filled.DarkMode
        "DeliveryDining" -> Icons.Filled.DeliveryDining
        "Directions" -> Icons.Filled.Directions
        "DirectionsBike" -> Icons.Filled.DirectionsBike
        "DirectionsCar" -> Icons.Filled.DirectionsCar
        "DirectionsRun" -> Icons.Filled.DirectionsRun
        "DirectionsWalk" -> Icons.Filled.DirectionsWalk
        "DoNotDisturbOff" -> Icons.Filled.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.Filled.DoNotDisturbOn
        "Done" -> Icons.Filled.Done
        "Download" -> Icons.Filled.Download
        "Downloading" -> Icons.Filled.Downloading
        "Earbuds" -> Icons.Filled.Earbuds
        "Eco" -> Icons.Filled.Eco
        "ElectricBolt" -> Icons.Filled.ElectricBolt
        "EmojiEmotions" -> Icons.Filled.EmojiEmotions
        "EmojiObjects" -> Icons.Filled.EmojiObjects
        "EnergySavingsLeaf" -> Icons.Filled.EnergySavingsLeaf
        "Equalizer" -> Icons.Filled.Equalizer
        "Error" -> Icons.Filled.Error
        "Event" -> Icons.Filled.Event
        "Face" -> Icons.Filled.Face
        "Fastfood" -> Icons.Filled.Fastfood
        "Favorite" -> Icons.Filled.Favorite
        "FiberManualRecord" -> Icons.Filled.FiberManualRecord
        "Fingerprint" -> Icons.Filled.Fingerprint
        "FitnessCenter" -> Icons.Filled.FitnessCenter
        "Flag" -> Icons.Filled.Flag
        "FlashlightOff" -> Icons.Filled.FlashlightOff
        "FlashlightOn" -> Icons.Filled.FlashlightOn
        "Flight" -> Icons.Filled.Flight
        "FlightLand" -> Icons.Filled.FlightLand
        "FlightTakeoff" -> Icons.Filled.FlightTakeoff
        "Forest" -> Icons.Filled.Forest
        "Forum" -> Icons.Filled.Forum
        "Games" -> Icons.Filled.Games
        "Grain" -> Icons.Filled.Grain
        "GraphicEq" -> Icons.Filled.GraphicEq
        "Grass" -> Icons.Filled.Grass
        "Headphones" -> Icons.Filled.Headphones
        "Headset" -> Icons.Filled.Headset
        "HeadsetMic" -> Icons.Filled.HeadsetMic
        "HeadsetOff" -> Icons.Filled.HeadsetOff
        "Help" -> Icons.Filled.Help
        "Highlight" -> Icons.Filled.Highlight
        "Home" -> Icons.Filled.Home
        "HourglassBottom" -> Icons.Filled.HourglassBottom
        "HourglassTop" -> Icons.Filled.HourglassTop
        "Info" -> Icons.Filled.Info
        "Key" -> Icons.Filled.Key
        "Language" -> Icons.Filled.Language
        "LibraryMusic" -> Icons.Filled.LibraryMusic
        "LightMode" -> Icons.Filled.LightMode
        "Lightbulb" -> Icons.Filled.Lightbulb
        "LocalCafe" -> Icons.Filled.LocalCafe
        "LocalFireDepartment" -> Icons.Filled.LocalFireDepartment
        "LocalShipping" -> Icons.Filled.LocalShipping
        "LocalTaxi" -> Icons.Filled.LocalTaxi
        "Lock" -> Icons.Filled.Lock
        "LockOpen" -> Icons.Filled.LockOpen
        "Mail" -> Icons.Filled.Mail
        "Map" -> Icons.Filled.Map
        "Message" -> Icons.Filled.Message
        "Mic" -> Icons.Filled.Mic
        "Mood" -> Icons.Filled.Mood
        "MusicNote" -> Icons.Filled.MusicNote
        "MusicOff" -> Icons.Filled.MusicOff
        "Navigation" -> Icons.Filled.Navigation
        "ContentCopy" -> Icons.Filled.ContentCopy
        "BrightnessMedium" -> Icons.Filled.BrightnessMedium
        "ScreenRotation" -> Icons.Filled.ScreenRotation
        "ScreenLockRotation" -> Icons.Filled.ScreenLockRotation
        "SignalWifiOff" -> Icons.Filled.SignalWifiOff
        "SignalWifi4Bar" -> Icons.Filled.SignalWifi4Bar
        "Share" -> Icons.Filled.Share
        "Inventory2" -> Icons.Filled.Inventory2
        "SignalCellularOff" -> Icons.Filled.SignalCellularOff
        "Speed" -> Icons.Filled.Speed
        "TurnLeft" -> Icons.Filled.TurnLeft
        "TurnRight" -> Icons.Filled.TurnRight
        "Straight" -> Icons.Filled.Straight
        "CurrencyExchange" -> Icons.Filled.CurrencyExchange
        "ShowChart" -> Icons.Filled.ShowChart
        "Layers" -> Icons.Filled.Layers
        "PhoneMissed" -> Icons.Filled.PhoneMissed
        "KeyboardVoice" -> Icons.Filled.KeyboardVoice
        "Nightlight" -> Icons.Filled.Nightlight
        "CurrencyBitcoin" -> Icons.Filled.CurrencyBitcoin
        "Wallpaper" -> Icons.Filled.Wallpaper
        "NearMe" -> Icons.Filled.NearMe
        "NetworkCheck" -> Icons.Filled.NetworkCheck
        "NightsStay" -> Icons.Filled.NightsStay
        "Notifications" -> Icons.Filled.Notifications
        "NotificationsActive" -> Icons.Filled.NotificationsActive
        "NotificationsOff" -> Icons.Filled.NotificationsOff
        "Park" -> Icons.Filled.Park
        "Payments" -> Icons.Filled.Payments
        "Pets" -> Icons.Filled.Pets
        "PhoneCallback" -> Icons.Filled.PhoneCallback
        "PhoneInTalk" -> Icons.Filled.PhoneInTalk
        "PhotoCamera" -> Icons.Filled.PhotoCamera
        "Place" -> Icons.Filled.Place
        "Podcasts" -> Icons.Filled.Podcasts
        "Power" -> Icons.Filled.Power
        "PowerSettingsNew" -> Icons.Filled.PowerSettingsNew
        "Psychology" -> Icons.Filled.Psychology
        "Public" -> Icons.Filled.Public
        "Radio" -> Icons.Filled.Radio
        "Refresh" -> Icons.Filled.Refresh
        "Reply" -> Icons.Filled.Reply
        "Restaurant" -> Icons.Filled.Restaurant
        "Rocket" -> Icons.Filled.Rocket
        "RocketLaunch" -> Icons.Filled.RocketLaunch
        "Router" -> Icons.Filled.Router
        "Savings" -> Icons.Filled.Savings
        "Schedule" -> Icons.Filled.Schedule
        "School" -> Icons.Filled.School
        "Screenshot" -> Icons.Filled.Screenshot
        "Security" -> Icons.Filled.Security
        "SelfImprovement" -> Icons.Filled.SelfImprovement
        "Send" -> Icons.Filled.Send
        "SentimentSatisfied" -> Icons.Filled.SentimentSatisfied
        "Shield" -> Icons.Filled.Shield
        "ShoppingBag" -> Icons.Filled.ShoppingBag
        "ShoppingCart" -> Icons.Filled.ShoppingCart
        "SignalCellularAlt" -> Icons.Filled.SignalCellularAlt
        "Snooze" -> Icons.Filled.Snooze
        "Spa" -> Icons.Filled.Spa
        "Speaker" -> Icons.Filled.Speaker
        "SportsEsports" -> Icons.Filled.SportsEsports
        "SportsScore" -> Icons.Filled.SportsScore
        "SportsSoccer" -> Icons.Filled.SportsSoccer
        "Star" -> Icons.Filled.Star
        "Sync" -> Icons.Filled.Sync
        "Thermostat" -> Icons.Filled.Thermostat
        "Thunderstorm" -> Icons.Filled.Thunderstorm
        "Timer" -> Icons.Filled.Timer
        "Toys" -> Icons.Filled.Toys
        "Umbrella" -> Icons.Filled.Umbrella
        "Upload" -> Icons.Filled.Upload
        "VerifiedUser" -> Icons.Filled.VerifiedUser
        "Vibration" -> Icons.Filled.Vibration
        "Videocam" -> Icons.Filled.Videocam
        "VolumeMute" -> Icons.Filled.VolumeMute
        "VolumeOff" -> Icons.Filled.VolumeOff
        "VolumeUp" -> Icons.Filled.VolumeUp
        "VpnKey" -> Icons.Filled.VpnKey
        "VpnKeyOff" -> Icons.Filled.VpnKeyOff
        "Warning" -> Icons.Filled.Warning
        "Watch" -> Icons.Filled.Watch
        "Water" -> Icons.Filled.Water
        "WaterDrop" -> Icons.Filled.WaterDrop
        "WbCloudy" -> Icons.Filled.WbCloudy
        "WbSunny" -> Icons.Filled.WbSunny
        "Whatshot" -> Icons.Filled.Whatshot
        "Wifi" -> Icons.Filled.Wifi
        "WifiOff" -> Icons.Filled.WifiOff
        "WifiTethering" -> Icons.Filled.WifiTethering
        "WifiTetheringOff" -> Icons.Filled.WifiTetheringOff
        "Work" -> Icons.Filled.Work
        else -> null
    }
    IconVariant.OUTLINED -> when (name) {
        "AcUnit" -> Icons.Outlined.AcUnit
        "Air" -> Icons.Outlined.Air
        "AirplanemodeActive" -> Icons.Outlined.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.Outlined.AirplanemodeInactive
        "Alarm" -> Icons.Outlined.Alarm
        "AlarmOn" -> Icons.Outlined.AlarmOn
        "Album" -> Icons.Outlined.Album
        "AutoAwesome" -> Icons.Outlined.AutoAwesome
        "AvTimer" -> Icons.Outlined.AvTimer
        "Battery2Bar" -> Icons.Outlined.Battery2Bar
        "Battery5Bar" -> Icons.Outlined.Battery5Bar
        "BatteryAlert" -> Icons.Outlined.BatteryAlert
        "BatteryChargingFull" -> Icons.Outlined.BatteryChargingFull
        "BatteryFull" -> Icons.Outlined.BatteryFull
        "BatterySaver" -> Icons.Outlined.BatterySaver
        "BatteryStd" -> Icons.Outlined.BatteryStd
        "Bedtime" -> Icons.Outlined.Bedtime
        "Bluetooth" -> Icons.Outlined.Bluetooth
        "BluetoothAudio" -> Icons.Outlined.BluetoothAudio
        "BluetoothConnected" -> Icons.Outlined.BluetoothConnected
        "BluetoothDisabled" -> Icons.Outlined.BluetoothDisabled
        "Bolt" -> Icons.Outlined.Bolt
        "Brightness5" -> Icons.Outlined.Brightness5
        "Cake" -> Icons.Outlined.Cake
        "Call" -> Icons.Outlined.Call
        "Celebration" -> Icons.Outlined.Celebration
        "Chat" -> Icons.Outlined.Chat
        "Check" -> Icons.Outlined.Check
        "CheckCircle" -> Icons.Outlined.CheckCircle
        "Cloud" -> Icons.Outlined.Cloud
        "CloudDone" -> Icons.Outlined.CloudDone
        "CloudDownload" -> Icons.Outlined.CloudDownload
        "CloudQueue" -> Icons.Outlined.CloudQueue
        "CloudUpload" -> Icons.Outlined.CloudUpload
        "Coffee" -> Icons.Outlined.Coffee
        "CreditCard" -> Icons.Outlined.CreditCard
        "DarkMode" -> Icons.Outlined.DarkMode
        "DeliveryDining" -> Icons.Outlined.DeliveryDining
        "Directions" -> Icons.Outlined.Directions
        "DirectionsBike" -> Icons.Outlined.DirectionsBike
        "DirectionsCar" -> Icons.Outlined.DirectionsCar
        "DirectionsRun" -> Icons.Outlined.DirectionsRun
        "DirectionsWalk" -> Icons.Outlined.DirectionsWalk
        "DoNotDisturbOff" -> Icons.Outlined.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.Outlined.DoNotDisturbOn
        "Done" -> Icons.Outlined.Done
        "Download" -> Icons.Outlined.Download
        "Downloading" -> Icons.Outlined.Downloading
        "Earbuds" -> Icons.Outlined.Earbuds
        "Eco" -> Icons.Outlined.Eco
        "ElectricBolt" -> Icons.Outlined.ElectricBolt
        "EmojiEmotions" -> Icons.Outlined.EmojiEmotions
        "EmojiObjects" -> Icons.Outlined.EmojiObjects
        "EnergySavingsLeaf" -> Icons.Outlined.EnergySavingsLeaf
        "Equalizer" -> Icons.Outlined.Equalizer
        "Error" -> Icons.Outlined.Error
        "Event" -> Icons.Outlined.Event
        "Face" -> Icons.Outlined.Face
        "Fastfood" -> Icons.Outlined.Fastfood
        "Favorite" -> Icons.Outlined.Favorite
        "FiberManualRecord" -> Icons.Outlined.FiberManualRecord
        "Fingerprint" -> Icons.Outlined.Fingerprint
        "FitnessCenter" -> Icons.Outlined.FitnessCenter
        "Flag" -> Icons.Outlined.Flag
        "FlashlightOff" -> Icons.Outlined.FlashlightOff
        "FlashlightOn" -> Icons.Outlined.FlashlightOn
        "Flight" -> Icons.Outlined.Flight
        "FlightLand" -> Icons.Outlined.FlightLand
        "FlightTakeoff" -> Icons.Outlined.FlightTakeoff
        "Forest" -> Icons.Outlined.Forest
        "Forum" -> Icons.Outlined.Forum
        "Games" -> Icons.Outlined.Games
        "Grain" -> Icons.Outlined.Grain
        "GraphicEq" -> Icons.Outlined.GraphicEq
        "Grass" -> Icons.Outlined.Grass
        "Headphones" -> Icons.Outlined.Headphones
        "Headset" -> Icons.Outlined.Headset
        "HeadsetMic" -> Icons.Outlined.HeadsetMic
        "HeadsetOff" -> Icons.Outlined.HeadsetOff
        "Help" -> Icons.Outlined.Help
        "Highlight" -> Icons.Outlined.Highlight
        "Home" -> Icons.Outlined.Home
        "HourglassBottom" -> Icons.Outlined.HourglassBottom
        "HourglassTop" -> Icons.Outlined.HourglassTop
        "Info" -> Icons.Outlined.Info
        "Key" -> Icons.Outlined.Key
        "Language" -> Icons.Outlined.Language
        "LibraryMusic" -> Icons.Outlined.LibraryMusic
        "LightMode" -> Icons.Outlined.LightMode
        "Lightbulb" -> Icons.Outlined.Lightbulb
        "LocalCafe" -> Icons.Outlined.LocalCafe
        "LocalFireDepartment" -> Icons.Outlined.LocalFireDepartment
        "LocalShipping" -> Icons.Outlined.LocalShipping
        "LocalTaxi" -> Icons.Outlined.LocalTaxi
        "Lock" -> Icons.Outlined.Lock
        "LockOpen" -> Icons.Outlined.LockOpen
        "Mail" -> Icons.Outlined.Mail
        "Map" -> Icons.Outlined.Map
        "Message" -> Icons.Outlined.Message
        "Mic" -> Icons.Outlined.Mic
        "Mood" -> Icons.Outlined.Mood
        "MusicNote" -> Icons.Outlined.MusicNote
        "MusicOff" -> Icons.Outlined.MusicOff
        "Navigation" -> Icons.Outlined.Navigation
        "ContentCopy" -> Icons.Outlined.ContentCopy
        "BrightnessMedium" -> Icons.Outlined.BrightnessMedium
        "ScreenRotation" -> Icons.Outlined.ScreenRotation
        "ScreenLockRotation" -> Icons.Outlined.ScreenLockRotation
        "SignalWifiOff" -> Icons.Outlined.SignalWifiOff
        "SignalWifi4Bar" -> Icons.Outlined.SignalWifi4Bar
        "Share" -> Icons.Outlined.Share
        "Inventory2" -> Icons.Outlined.Inventory2
        "SignalCellularOff" -> Icons.Outlined.SignalCellularOff
        "Speed" -> Icons.Outlined.Speed
        "TurnLeft" -> Icons.Outlined.TurnLeft
        "TurnRight" -> Icons.Outlined.TurnRight
        "Straight" -> Icons.Outlined.Straight
        "CurrencyExchange" -> Icons.Outlined.CurrencyExchange
        "ShowChart" -> Icons.Outlined.ShowChart
        "Layers" -> Icons.Outlined.Layers
        "PhoneMissed" -> Icons.Outlined.PhoneMissed
        "KeyboardVoice" -> Icons.Outlined.KeyboardVoice
        "Nightlight" -> Icons.Outlined.Nightlight
        "CurrencyBitcoin" -> Icons.Outlined.CurrencyBitcoin
        "Wallpaper" -> Icons.Outlined.Wallpaper
        "NearMe" -> Icons.Outlined.NearMe
        "NetworkCheck" -> Icons.Outlined.NetworkCheck
        "NightsStay" -> Icons.Outlined.NightsStay
        "Notifications" -> Icons.Outlined.Notifications
        "NotificationsActive" -> Icons.Outlined.NotificationsActive
        "NotificationsOff" -> Icons.Outlined.NotificationsOff
        "Park" -> Icons.Outlined.Park
        "Payments" -> Icons.Outlined.Payments
        "Pets" -> Icons.Outlined.Pets
        "PhoneCallback" -> Icons.Outlined.PhoneCallback
        "PhoneInTalk" -> Icons.Outlined.PhoneInTalk
        "PhotoCamera" -> Icons.Outlined.PhotoCamera
        "Place" -> Icons.Outlined.Place
        "Podcasts" -> Icons.Outlined.Podcasts
        "Power" -> Icons.Outlined.Power
        "PowerSettingsNew" -> Icons.Outlined.PowerSettingsNew
        "Psychology" -> Icons.Outlined.Psychology
        "Public" -> Icons.Outlined.Public
        "Radio" -> Icons.Outlined.Radio
        "Refresh" -> Icons.Outlined.Refresh
        "Reply" -> Icons.Outlined.Reply
        "Restaurant" -> Icons.Outlined.Restaurant
        "Rocket" -> Icons.Outlined.Rocket
        "RocketLaunch" -> Icons.Outlined.RocketLaunch
        "Router" -> Icons.Outlined.Router
        "Savings" -> Icons.Outlined.Savings
        "Schedule" -> Icons.Outlined.Schedule
        "School" -> Icons.Outlined.School
        "Screenshot" -> Icons.Outlined.Screenshot
        "Security" -> Icons.Outlined.Security
        "SelfImprovement" -> Icons.Outlined.SelfImprovement
        "Send" -> Icons.Outlined.Send
        "SentimentSatisfied" -> Icons.Outlined.SentimentSatisfied
        "Shield" -> Icons.Outlined.Shield
        "ShoppingBag" -> Icons.Outlined.ShoppingBag
        "ShoppingCart" -> Icons.Outlined.ShoppingCart
        "SignalCellularAlt" -> Icons.Outlined.SignalCellularAlt
        "Snooze" -> Icons.Outlined.Snooze
        "Spa" -> Icons.Outlined.Spa
        "Speaker" -> Icons.Outlined.Speaker
        "SportsEsports" -> Icons.Outlined.SportsEsports
        "SportsScore" -> Icons.Outlined.SportsScore
        "SportsSoccer" -> Icons.Outlined.SportsSoccer
        "Star" -> Icons.Outlined.Star
        "Sync" -> Icons.Outlined.Sync
        "Thermostat" -> Icons.Outlined.Thermostat
        "Thunderstorm" -> Icons.Outlined.Thunderstorm
        "Timer" -> Icons.Outlined.Timer
        "Toys" -> Icons.Outlined.Toys
        "Umbrella" -> Icons.Outlined.Umbrella
        "Upload" -> Icons.Outlined.Upload
        "VerifiedUser" -> Icons.Outlined.VerifiedUser
        "Vibration" -> Icons.Outlined.Vibration
        "Videocam" -> Icons.Outlined.Videocam
        "VolumeMute" -> Icons.Outlined.VolumeMute
        "VolumeOff" -> Icons.Outlined.VolumeOff
        "VolumeUp" -> Icons.Outlined.VolumeUp
        "VpnKey" -> Icons.Outlined.VpnKey
        "VpnKeyOff" -> Icons.Outlined.VpnKeyOff
        "Warning" -> Icons.Outlined.Warning
        "Watch" -> Icons.Outlined.Watch
        "Water" -> Icons.Outlined.Water
        "WaterDrop" -> Icons.Outlined.WaterDrop
        "WbCloudy" -> Icons.Outlined.WbCloudy
        "WbSunny" -> Icons.Outlined.WbSunny
        "Whatshot" -> Icons.Outlined.Whatshot
        "Wifi" -> Icons.Outlined.Wifi
        "WifiOff" -> Icons.Outlined.WifiOff
        "WifiTethering" -> Icons.Outlined.WifiTethering
        "WifiTetheringOff" -> Icons.Outlined.WifiTetheringOff
        "Work" -> Icons.Outlined.Work
        else -> null
    }
    IconVariant.SHARP -> when (name) {
        "AcUnit" -> Icons.Sharp.AcUnit
        "Air" -> Icons.Sharp.Air
        "AirplanemodeActive" -> Icons.Sharp.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.Sharp.AirplanemodeInactive
        "Alarm" -> Icons.Sharp.Alarm
        "AlarmOn" -> Icons.Sharp.AlarmOn
        "Album" -> Icons.Sharp.Album
        "AutoAwesome" -> Icons.Sharp.AutoAwesome
        "AvTimer" -> Icons.Sharp.AvTimer
        "Battery2Bar" -> Icons.Sharp.Battery2Bar
        "Battery5Bar" -> Icons.Sharp.Battery5Bar
        "BatteryAlert" -> Icons.Sharp.BatteryAlert
        "BatteryChargingFull" -> Icons.Sharp.BatteryChargingFull
        "BatteryFull" -> Icons.Sharp.BatteryFull
        "BatterySaver" -> Icons.Sharp.BatterySaver
        "BatteryStd" -> Icons.Sharp.BatteryStd
        "Bedtime" -> Icons.Sharp.Bedtime
        "Bluetooth" -> Icons.Sharp.Bluetooth
        "BluetoothAudio" -> Icons.Sharp.BluetoothAudio
        "BluetoothConnected" -> Icons.Sharp.BluetoothConnected
        "BluetoothDisabled" -> Icons.Sharp.BluetoothDisabled
        "Bolt" -> Icons.Sharp.Bolt
        "Brightness5" -> Icons.Sharp.Brightness5
        "Cake" -> Icons.Sharp.Cake
        "Call" -> Icons.Sharp.Call
        "Celebration" -> Icons.Sharp.Celebration
        "Chat" -> Icons.Sharp.Chat
        "Check" -> Icons.Sharp.Check
        "CheckCircle" -> Icons.Sharp.CheckCircle
        "Cloud" -> Icons.Sharp.Cloud
        "CloudDone" -> Icons.Sharp.CloudDone
        "CloudDownload" -> Icons.Sharp.CloudDownload
        "CloudQueue" -> Icons.Sharp.CloudQueue
        "CloudUpload" -> Icons.Sharp.CloudUpload
        "Coffee" -> Icons.Sharp.Coffee
        "CreditCard" -> Icons.Sharp.CreditCard
        "DarkMode" -> Icons.Sharp.DarkMode
        "DeliveryDining" -> Icons.Sharp.DeliveryDining
        "Directions" -> Icons.Sharp.Directions
        "DirectionsBike" -> Icons.Sharp.DirectionsBike
        "DirectionsCar" -> Icons.Sharp.DirectionsCar
        "DirectionsRun" -> Icons.Sharp.DirectionsRun
        "DirectionsWalk" -> Icons.Sharp.DirectionsWalk
        "DoNotDisturbOff" -> Icons.Sharp.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.Sharp.DoNotDisturbOn
        "Done" -> Icons.Sharp.Done
        "Download" -> Icons.Sharp.Download
        "Downloading" -> Icons.Sharp.Downloading
        "Earbuds" -> Icons.Sharp.Earbuds
        "Eco" -> Icons.Sharp.Eco
        "ElectricBolt" -> Icons.Sharp.ElectricBolt
        "EmojiEmotions" -> Icons.Sharp.EmojiEmotions
        "EmojiObjects" -> Icons.Sharp.EmojiObjects
        "EnergySavingsLeaf" -> Icons.Sharp.EnergySavingsLeaf
        "Equalizer" -> Icons.Sharp.Equalizer
        "Error" -> Icons.Sharp.Error
        "Event" -> Icons.Sharp.Event
        "Face" -> Icons.Sharp.Face
        "Fastfood" -> Icons.Sharp.Fastfood
        "Favorite" -> Icons.Sharp.Favorite
        "FiberManualRecord" -> Icons.Sharp.FiberManualRecord
        "Fingerprint" -> Icons.Sharp.Fingerprint
        "FitnessCenter" -> Icons.Sharp.FitnessCenter
        "Flag" -> Icons.Sharp.Flag
        "FlashlightOff" -> Icons.Sharp.FlashlightOff
        "FlashlightOn" -> Icons.Sharp.FlashlightOn
        "Flight" -> Icons.Sharp.Flight
        "FlightLand" -> Icons.Sharp.FlightLand
        "FlightTakeoff" -> Icons.Sharp.FlightTakeoff
        "Forest" -> Icons.Sharp.Forest
        "Forum" -> Icons.Sharp.Forum
        "Games" -> Icons.Sharp.Games
        "Grain" -> Icons.Sharp.Grain
        "GraphicEq" -> Icons.Sharp.GraphicEq
        "Grass" -> Icons.Sharp.Grass
        "Headphones" -> Icons.Sharp.Headphones
        "Headset" -> Icons.Sharp.Headset
        "HeadsetMic" -> Icons.Sharp.HeadsetMic
        "HeadsetOff" -> Icons.Sharp.HeadsetOff
        "Help" -> Icons.Sharp.Help
        "Highlight" -> Icons.Sharp.Highlight
        "Home" -> Icons.Sharp.Home
        "HourglassBottom" -> Icons.Sharp.HourglassBottom
        "HourglassTop" -> Icons.Sharp.HourglassTop
        "Info" -> Icons.Sharp.Info
        "Key" -> Icons.Sharp.Key
        "Language" -> Icons.Sharp.Language
        "LibraryMusic" -> Icons.Sharp.LibraryMusic
        "LightMode" -> Icons.Sharp.LightMode
        "Lightbulb" -> Icons.Sharp.Lightbulb
        "LocalCafe" -> Icons.Sharp.LocalCafe
        "LocalFireDepartment" -> Icons.Sharp.LocalFireDepartment
        "LocalShipping" -> Icons.Sharp.LocalShipping
        "LocalTaxi" -> Icons.Sharp.LocalTaxi
        "Lock" -> Icons.Sharp.Lock
        "LockOpen" -> Icons.Sharp.LockOpen
        "Mail" -> Icons.Sharp.Mail
        "Map" -> Icons.Sharp.Map
        "Message" -> Icons.Sharp.Message
        "Mic" -> Icons.Sharp.Mic
        "Mood" -> Icons.Sharp.Mood
        "MusicNote" -> Icons.Sharp.MusicNote
        "MusicOff" -> Icons.Sharp.MusicOff
        "Navigation" -> Icons.Sharp.Navigation
        "ContentCopy" -> Icons.Sharp.ContentCopy
        "BrightnessMedium" -> Icons.Sharp.BrightnessMedium
        "ScreenRotation" -> Icons.Sharp.ScreenRotation
        "ScreenLockRotation" -> Icons.Sharp.ScreenLockRotation
        "SignalWifiOff" -> Icons.Sharp.SignalWifiOff
        "SignalWifi4Bar" -> Icons.Sharp.SignalWifi4Bar
        "Share" -> Icons.Sharp.Share
        "Inventory2" -> Icons.Sharp.Inventory2
        "SignalCellularOff" -> Icons.Sharp.SignalCellularOff
        "Speed" -> Icons.Sharp.Speed
        "TurnLeft" -> Icons.Sharp.TurnLeft
        "TurnRight" -> Icons.Sharp.TurnRight
        "Straight" -> Icons.Sharp.Straight
        "CurrencyExchange" -> Icons.Sharp.CurrencyExchange
        "ShowChart" -> Icons.Sharp.ShowChart
        "Layers" -> Icons.Sharp.Layers
        "PhoneMissed" -> Icons.Sharp.PhoneMissed
        "KeyboardVoice" -> Icons.Sharp.KeyboardVoice
        "Nightlight" -> Icons.Sharp.Nightlight
        "CurrencyBitcoin" -> Icons.Sharp.CurrencyBitcoin
        "Wallpaper" -> Icons.Sharp.Wallpaper
        "NearMe" -> Icons.Sharp.NearMe
        "NetworkCheck" -> Icons.Sharp.NetworkCheck
        "NightsStay" -> Icons.Sharp.NightsStay
        "Notifications" -> Icons.Sharp.Notifications
        "NotificationsActive" -> Icons.Sharp.NotificationsActive
        "NotificationsOff" -> Icons.Sharp.NotificationsOff
        "Park" -> Icons.Sharp.Park
        "Payments" -> Icons.Sharp.Payments
        "Pets" -> Icons.Sharp.Pets
        "PhoneCallback" -> Icons.Sharp.PhoneCallback
        "PhoneInTalk" -> Icons.Sharp.PhoneInTalk
        "PhotoCamera" -> Icons.Sharp.PhotoCamera
        "Place" -> Icons.Sharp.Place
        "Podcasts" -> Icons.Sharp.Podcasts
        "Power" -> Icons.Sharp.Power
        "PowerSettingsNew" -> Icons.Sharp.PowerSettingsNew
        "Psychology" -> Icons.Sharp.Psychology
        "Public" -> Icons.Sharp.Public
        "Radio" -> Icons.Sharp.Radio
        "Refresh" -> Icons.Sharp.Refresh
        "Reply" -> Icons.Sharp.Reply
        "Restaurant" -> Icons.Sharp.Restaurant
        "Rocket" -> Icons.Sharp.Rocket
        "RocketLaunch" -> Icons.Sharp.RocketLaunch
        "Router" -> Icons.Sharp.Router
        "Savings" -> Icons.Sharp.Savings
        "Schedule" -> Icons.Sharp.Schedule
        "School" -> Icons.Sharp.School
        "Screenshot" -> Icons.Sharp.Screenshot
        "Security" -> Icons.Sharp.Security
        "SelfImprovement" -> Icons.Sharp.SelfImprovement
        "Send" -> Icons.Sharp.Send
        "SentimentSatisfied" -> Icons.Sharp.SentimentSatisfied
        "Shield" -> Icons.Sharp.Shield
        "ShoppingBag" -> Icons.Sharp.ShoppingBag
        "ShoppingCart" -> Icons.Sharp.ShoppingCart
        "SignalCellularAlt" -> Icons.Sharp.SignalCellularAlt
        "Snooze" -> Icons.Sharp.Snooze
        "Spa" -> Icons.Sharp.Spa
        "Speaker" -> Icons.Sharp.Speaker
        "SportsEsports" -> Icons.Sharp.SportsEsports
        "SportsScore" -> Icons.Sharp.SportsScore
        "SportsSoccer" -> Icons.Sharp.SportsSoccer
        "Star" -> Icons.Sharp.Star
        "Sync" -> Icons.Sharp.Sync
        "Thermostat" -> Icons.Sharp.Thermostat
        "Thunderstorm" -> Icons.Sharp.Thunderstorm
        "Timer" -> Icons.Sharp.Timer
        "Toys" -> Icons.Sharp.Toys
        "Umbrella" -> Icons.Sharp.Umbrella
        "Upload" -> Icons.Sharp.Upload
        "VerifiedUser" -> Icons.Sharp.VerifiedUser
        "Vibration" -> Icons.Sharp.Vibration
        "Videocam" -> Icons.Sharp.Videocam
        "VolumeMute" -> Icons.Sharp.VolumeMute
        "VolumeOff" -> Icons.Sharp.VolumeOff
        "VolumeUp" -> Icons.Sharp.VolumeUp
        "VpnKey" -> Icons.Sharp.VpnKey
        "VpnKeyOff" -> Icons.Sharp.VpnKeyOff
        "Warning" -> Icons.Sharp.Warning
        "Watch" -> Icons.Sharp.Watch
        "Water" -> Icons.Sharp.Water
        "WaterDrop" -> Icons.Sharp.WaterDrop
        "WbCloudy" -> Icons.Sharp.WbCloudy
        "WbSunny" -> Icons.Sharp.WbSunny
        "Whatshot" -> Icons.Sharp.Whatshot
        "Wifi" -> Icons.Sharp.Wifi
        "WifiOff" -> Icons.Sharp.WifiOff
        "WifiTethering" -> Icons.Sharp.WifiTethering
        "WifiTetheringOff" -> Icons.Sharp.WifiTetheringOff
        "Work" -> Icons.Sharp.Work
        else -> null
    }
    IconVariant.TWOTONE -> when (name) {
        "AcUnit" -> Icons.TwoTone.AcUnit
        "Air" -> Icons.TwoTone.Air
        "AirplanemodeActive" -> Icons.TwoTone.AirplanemodeActive
        "AirplanemodeInactive" -> Icons.TwoTone.AirplanemodeInactive
        "Alarm" -> Icons.TwoTone.Alarm
        "AlarmOn" -> Icons.TwoTone.AlarmOn
        "Album" -> Icons.TwoTone.Album
        "AutoAwesome" -> Icons.TwoTone.AutoAwesome
        "AvTimer" -> Icons.TwoTone.AvTimer
        "Battery2Bar" -> Icons.TwoTone.Battery2Bar
        "Battery5Bar" -> Icons.TwoTone.Battery5Bar
        "BatteryAlert" -> Icons.TwoTone.BatteryAlert
        "BatteryChargingFull" -> Icons.TwoTone.BatteryChargingFull
        "BatteryFull" -> Icons.TwoTone.BatteryFull
        "BatterySaver" -> Icons.TwoTone.BatterySaver
        "BatteryStd" -> Icons.TwoTone.BatteryStd
        "Bedtime" -> Icons.TwoTone.Bedtime
        "Bluetooth" -> Icons.TwoTone.Bluetooth
        "BluetoothAudio" -> Icons.TwoTone.BluetoothAudio
        "BluetoothConnected" -> Icons.TwoTone.BluetoothConnected
        "BluetoothDisabled" -> Icons.TwoTone.BluetoothDisabled
        "Bolt" -> Icons.TwoTone.Bolt
        "Brightness5" -> Icons.TwoTone.Brightness5
        "Cake" -> Icons.TwoTone.Cake
        "Call" -> Icons.TwoTone.Call
        "Celebration" -> Icons.TwoTone.Celebration
        "Chat" -> Icons.TwoTone.Chat
        "Check" -> Icons.TwoTone.Check
        "CheckCircle" -> Icons.TwoTone.CheckCircle
        "Cloud" -> Icons.TwoTone.Cloud
        "CloudDone" -> Icons.TwoTone.CloudDone
        "CloudDownload" -> Icons.TwoTone.CloudDownload
        "CloudQueue" -> Icons.TwoTone.CloudQueue
        "CloudUpload" -> Icons.TwoTone.CloudUpload
        "Coffee" -> Icons.TwoTone.Coffee
        "CreditCard" -> Icons.TwoTone.CreditCard
        "DarkMode" -> Icons.TwoTone.DarkMode
        "DeliveryDining" -> Icons.TwoTone.DeliveryDining
        "Directions" -> Icons.TwoTone.Directions
        "DirectionsBike" -> Icons.TwoTone.DirectionsBike
        "DirectionsCar" -> Icons.TwoTone.DirectionsCar
        "DirectionsRun" -> Icons.TwoTone.DirectionsRun
        "DirectionsWalk" -> Icons.TwoTone.DirectionsWalk
        "DoNotDisturbOff" -> Icons.TwoTone.DoNotDisturbOff
        "DoNotDisturbOn" -> Icons.TwoTone.DoNotDisturbOn
        "Done" -> Icons.TwoTone.Done
        "Download" -> Icons.TwoTone.Download
        "Downloading" -> Icons.TwoTone.Downloading
        "Earbuds" -> Icons.TwoTone.Earbuds
        "Eco" -> Icons.TwoTone.Eco
        "ElectricBolt" -> Icons.TwoTone.ElectricBolt
        "EmojiEmotions" -> Icons.TwoTone.EmojiEmotions
        "EmojiObjects" -> Icons.TwoTone.EmojiObjects
        "EnergySavingsLeaf" -> Icons.TwoTone.EnergySavingsLeaf
        "Equalizer" -> Icons.TwoTone.Equalizer
        "Error" -> Icons.TwoTone.Error
        "Event" -> Icons.TwoTone.Event
        "Face" -> Icons.TwoTone.Face
        "Fastfood" -> Icons.TwoTone.Fastfood
        "Favorite" -> Icons.TwoTone.Favorite
        "FiberManualRecord" -> Icons.TwoTone.FiberManualRecord
        "Fingerprint" -> Icons.TwoTone.Fingerprint
        "FitnessCenter" -> Icons.TwoTone.FitnessCenter
        "Flag" -> Icons.TwoTone.Flag
        "FlashlightOff" -> Icons.TwoTone.FlashlightOff
        "FlashlightOn" -> Icons.TwoTone.FlashlightOn
        "Flight" -> Icons.TwoTone.Flight
        "FlightLand" -> Icons.TwoTone.FlightLand
        "FlightTakeoff" -> Icons.TwoTone.FlightTakeoff
        "Forest" -> Icons.TwoTone.Forest
        "Forum" -> Icons.TwoTone.Forum
        "Games" -> Icons.TwoTone.Games
        "Grain" -> Icons.TwoTone.Grain
        "GraphicEq" -> Icons.TwoTone.GraphicEq
        "Grass" -> Icons.TwoTone.Grass
        "Headphones" -> Icons.TwoTone.Headphones
        "Headset" -> Icons.TwoTone.Headset
        "HeadsetMic" -> Icons.TwoTone.HeadsetMic
        "HeadsetOff" -> Icons.TwoTone.HeadsetOff
        "Help" -> Icons.TwoTone.Help
        "Highlight" -> Icons.TwoTone.Highlight
        "Home" -> Icons.TwoTone.Home
        "HourglassBottom" -> Icons.TwoTone.HourglassBottom
        "HourglassTop" -> Icons.TwoTone.HourglassTop
        "Info" -> Icons.TwoTone.Info
        "Key" -> Icons.TwoTone.Key
        "Language" -> Icons.TwoTone.Language
        "LibraryMusic" -> Icons.TwoTone.LibraryMusic
        "LightMode" -> Icons.TwoTone.LightMode
        "Lightbulb" -> Icons.TwoTone.Lightbulb
        "LocalCafe" -> Icons.TwoTone.LocalCafe
        "LocalFireDepartment" -> Icons.TwoTone.LocalFireDepartment
        "LocalShipping" -> Icons.TwoTone.LocalShipping
        "LocalTaxi" -> Icons.TwoTone.LocalTaxi
        "Lock" -> Icons.TwoTone.Lock
        "LockOpen" -> Icons.TwoTone.LockOpen
        "Mail" -> Icons.TwoTone.Mail
        "Map" -> Icons.TwoTone.Map
        "Message" -> Icons.TwoTone.Message
        "Mic" -> Icons.TwoTone.Mic
        "Mood" -> Icons.TwoTone.Mood
        "MusicNote" -> Icons.TwoTone.MusicNote
        "MusicOff" -> Icons.TwoTone.MusicOff
        "Navigation" -> Icons.TwoTone.Navigation
        "ContentCopy" -> Icons.TwoTone.ContentCopy
        "BrightnessMedium" -> Icons.TwoTone.BrightnessMedium
        "ScreenRotation" -> Icons.TwoTone.ScreenRotation
        "ScreenLockRotation" -> Icons.TwoTone.ScreenLockRotation
        "SignalWifiOff" -> Icons.TwoTone.SignalWifiOff
        "SignalWifi4Bar" -> Icons.TwoTone.SignalWifi4Bar
        "Share" -> Icons.TwoTone.Share
        "Inventory2" -> Icons.TwoTone.Inventory2
        "SignalCellularOff" -> Icons.TwoTone.SignalCellularOff
        "Speed" -> Icons.TwoTone.Speed
        "TurnLeft" -> Icons.TwoTone.TurnLeft
        "TurnRight" -> Icons.TwoTone.TurnRight
        "Straight" -> Icons.TwoTone.Straight
        "CurrencyExchange" -> Icons.TwoTone.CurrencyExchange
        "ShowChart" -> Icons.TwoTone.ShowChart
        "Layers" -> Icons.TwoTone.Layers
        "PhoneMissed" -> Icons.TwoTone.PhoneMissed
        "KeyboardVoice" -> Icons.TwoTone.KeyboardVoice
        "Nightlight" -> Icons.TwoTone.Nightlight
        "CurrencyBitcoin" -> Icons.TwoTone.CurrencyBitcoin
        "Wallpaper" -> Icons.TwoTone.Wallpaper
        "NearMe" -> Icons.TwoTone.NearMe
        "NetworkCheck" -> Icons.TwoTone.NetworkCheck
        "NightsStay" -> Icons.TwoTone.NightsStay
        "Notifications" -> Icons.TwoTone.Notifications
        "NotificationsActive" -> Icons.TwoTone.NotificationsActive
        "NotificationsOff" -> Icons.TwoTone.NotificationsOff
        "Park" -> Icons.TwoTone.Park
        "Payments" -> Icons.TwoTone.Payments
        "Pets" -> Icons.TwoTone.Pets
        "PhoneCallback" -> Icons.TwoTone.PhoneCallback
        "PhoneInTalk" -> Icons.TwoTone.PhoneInTalk
        "PhotoCamera" -> Icons.TwoTone.PhotoCamera
        "Place" -> Icons.TwoTone.Place
        "Podcasts" -> Icons.TwoTone.Podcasts
        "Power" -> Icons.TwoTone.Power
        "PowerSettingsNew" -> Icons.TwoTone.PowerSettingsNew
        "Psychology" -> Icons.TwoTone.Psychology
        "Public" -> Icons.TwoTone.Public
        "Radio" -> Icons.TwoTone.Radio
        "Refresh" -> Icons.TwoTone.Refresh
        "Reply" -> Icons.TwoTone.Reply
        "Restaurant" -> Icons.TwoTone.Restaurant
        "Rocket" -> Icons.TwoTone.Rocket
        "RocketLaunch" -> Icons.TwoTone.RocketLaunch
        "Router" -> Icons.TwoTone.Router
        "Savings" -> Icons.TwoTone.Savings
        "Schedule" -> Icons.TwoTone.Schedule
        "School" -> Icons.TwoTone.School
        "Screenshot" -> Icons.TwoTone.Screenshot
        "Security" -> Icons.TwoTone.Security
        "SelfImprovement" -> Icons.TwoTone.SelfImprovement
        "Send" -> Icons.TwoTone.Send
        "SentimentSatisfied" -> Icons.TwoTone.SentimentSatisfied
        "Shield" -> Icons.TwoTone.Shield
        "ShoppingBag" -> Icons.TwoTone.ShoppingBag
        "ShoppingCart" -> Icons.TwoTone.ShoppingCart
        "SignalCellularAlt" -> Icons.TwoTone.SignalCellularAlt
        "Snooze" -> Icons.TwoTone.Snooze
        "Spa" -> Icons.TwoTone.Spa
        "Speaker" -> Icons.TwoTone.Speaker
        "SportsEsports" -> Icons.TwoTone.SportsEsports
        "SportsScore" -> Icons.TwoTone.SportsScore
        "SportsSoccer" -> Icons.TwoTone.SportsSoccer
        "Star" -> Icons.TwoTone.Star
        "Sync" -> Icons.TwoTone.Sync
        "Thermostat" -> Icons.TwoTone.Thermostat
        "Thunderstorm" -> Icons.TwoTone.Thunderstorm
        "Timer" -> Icons.TwoTone.Timer
        "Toys" -> Icons.TwoTone.Toys
        "Umbrella" -> Icons.TwoTone.Umbrella
        "Upload" -> Icons.TwoTone.Upload
        "VerifiedUser" -> Icons.TwoTone.VerifiedUser
        "Vibration" -> Icons.TwoTone.Vibration
        "Videocam" -> Icons.TwoTone.Videocam
        "VolumeMute" -> Icons.TwoTone.VolumeMute
        "VolumeOff" -> Icons.TwoTone.VolumeOff
        "VolumeUp" -> Icons.TwoTone.VolumeUp
        "VpnKey" -> Icons.TwoTone.VpnKey
        "VpnKeyOff" -> Icons.TwoTone.VpnKeyOff
        "Warning" -> Icons.TwoTone.Warning
        "Watch" -> Icons.TwoTone.Watch
        "Water" -> Icons.TwoTone.Water
        "WaterDrop" -> Icons.TwoTone.WaterDrop
        "WbCloudy" -> Icons.TwoTone.WbCloudy
        "WbSunny" -> Icons.TwoTone.WbSunny
        "Whatshot" -> Icons.TwoTone.Whatshot
        "Wifi" -> Icons.TwoTone.Wifi
        "WifiOff" -> Icons.TwoTone.WifiOff
        "WifiTethering" -> Icons.TwoTone.WifiTethering
        "WifiTetheringOff" -> Icons.TwoTone.WifiTetheringOff
        "Work" -> Icons.TwoTone.Work
        else -> null
    }
}
