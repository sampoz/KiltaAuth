from django.db import models

class CardUsers(models.Model):
    created = models.DateTimeField(auto_now_add=True)
    name = models.CharField(max_length=100, blank=True, default='')
    cardId = models.TextField()
    active = models.BooleanField(default=True)
    appKey = models.CharField(max_length=100)

    class Meta:
        ordering = ('created',)
