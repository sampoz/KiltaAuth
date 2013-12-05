from django.forms import widgets
from rest_framework import serializers
from CardUsers.models import CardUsers
class CardUsersSerializer(serializers.ModelSerializer):
    class Meta:
        model = CardUsers
        fields = ('id', 'name', 'cardId', 'active', 'appKey')
