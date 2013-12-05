from django.conf.urls import patterns, url

urlpatterns = patterns('CardUsers.views',
    url(r'^CardUsers/$', 'CardUsers_list'),
    url(r'^CardUsers/detail/(?P<pk>.+)/$', 'CardUsers_detail'),
)
